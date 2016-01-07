package si.fri.adeserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import si.fri.algotest.analysis.DataAnalyser;
import si.fri.algotest.entities.Project;
import si.fri.algotest.global.ATGlobal;
import si.fri.algotest.global.ErrorStatus;
import si.fri.algotest.tools.ATTools;

/**
 *
 * @author tomaz
 */
public class ADETaskServer implements Runnable {
  // one array of tasks is shared by all servers
  public static Vector<ADETask> taskQueue;
  
  private Socket connection;
  private int ID;  // server ID


  public ADETaskServer(Socket connection, int count) {
    // when a server is created, array of tasks is read from file
    if (taskQueue == null)
      taskQueue = ADETools.readADETasks();
    
    this.connection = connection;
    this.ID = count;
  }
 
  
  
  /**
   * Create a new task and add it to waiting queue. 
   * Call: addTask project_name algorithm_name testset_name measurement_type
   * Return: task_id or error messsage if task can not be created.
   */
  public String addTask(ArrayList<String> params) {
    if (params.size() != 4)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String project = params.get(0), algorithm = params.get(1), testset = params.get(2), mType=params.get(3);
    ADETask task = new ADETask(project, algorithm, testset, mType, false); 
    
    if (task != null) {
      task.setCandidateComputers(ADETools.getCondidateComputersFor(task));
      
      taskQueue.add(task);
    
      ADETools.setTaskStatus(task, TaskStatus.QUEUED, null, null);
      Integer taskID = task.getField(ADETask.ID_TaskID);
      
      return taskID.toString();
    } else
      ADELog.log(ADEGlobal.ERROR_PREFIX + "AddTask " + params.toString());
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_ERROR_CREATE_TASK);
  }

  
  public String taskStatus(ArrayList<String> params) {
    if (params.size() != 1)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    for (ADETask task : taskQueue)
      if (task.toString().startsWith(params.get(0) + " "))
        return task.toString();
    
    return "Unknown task";
  }
  
  
  /**
   * Given task is String with four parameters (i.e. "Sorting_BubbleSort_TestSet1_em").
   * Method returns status of a task, which can be one of the following: QUEUED, RUNNING, 
   * DONE, NEW, OUTDATED, INCOMPLETE.
   * @return 
   */
  private String getStatusOfTask(Project project, String task) {
    String [] params = task.split("_");
    if (params.length != 4) return "UNKNOWN";
    
    ADETask tmpTask = new ADETask(params[0], params[1], params[2], params[3], true);
    int idx = taskQueue.indexOf(tmpTask);
    String statusLine = ADETools.getTaskStatus(tmpTask);
    
    String result = "";
    
    if (idx != -1) {
      ADETask tTask = taskQueue.get(idx);
      if (tTask.getTaskStatus().equals(TaskStatus.RUNNING)) {
        String stts = ADETask.HTML_TAG_RUNNING;
        
        // do i have information about % completed
        String delez = "";
        String status = ADETools.getTaskStatus(tTask);
        if (status != null) {
          String [] parts = status.split(" # ");
          if (parts.length > 3) 
            delez = parts[3];
        }
        stts = stts.replaceAll("!_!", delez);
                
        result = stts;
      } else           
        result = ADETask.HTML_TAG_QUEUED;
    } else {
      if (statusLine.isEmpty()) 
        result = ADETask.HTML_TAG_NEW;
      else {        
        result = ADETools.getTaskResultFileStatus(project, tmpTask);
      }                
    }
    return result.replaceAll("info", statusLine);
  }
  
  /**
   * Returns JSON string describing all tasks for a given request
   */
  public String projectStatus(ArrayList<String> params) {
    String result = "";
    if (params.size() < 1) return "{}";
    
    ArrayList<String> projectTasks = ADETools.getProjectTasks(params);
    
    
    Project project = new Project(ATGlobal.getALGatorDataRoot(), params.get(0));

    for (String projectTask : projectTasks) {
      result += (result.isEmpty() ? "" : ", ") + "\"" + projectTask + "\" : \"" + getStatusOfTask(project, projectTask) + "\"";
      
    }
    return "{" + result + "}";
  }
  
  /**
   * Finds the next task that can be executed on a computer with cid          <br>
   * Call: getNextTask cid                                                    <br>
   * Return: NO_TASKS or nextTask (id proj alg testset mtype)
   */
  private String getNextTask(ArrayList<String> params) {
    if (params.size() != 1)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String cid = params.get(0);

    ADETask task = null;
    for (ADETask aDETask : taskQueue) {
      if (aDETask.getTaskStatus().equals(TaskStatus.QUEUED) && aDETask.getCandidateComputers().contains(cid)) {
        task = aDETask; break;
      }
    }            
    
    if (task != null) {
        ADETools.setTaskStatus(task, TaskStatus.RUNNING, null, cid);
        ADETools.setComputerFamilyForProject(task, cid);
        return task.toString();
    } else
      return ADEGlobal.NO_TASKS;
  }
  
  /**
   * Removes task from tasksProcessed list and writes status to the file
   * Call: completeTask tid exitCode
   * Return: "" or error message
   */
  private String completeTask(ArrayList<String> params) {
    if (params.size() != 2)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String tid = params.get(0);
    ADETask task = null;
    
    // find a task 
      for (ADETask tsk: taskQueue) {
      if (tsk.toString().startsWith(tid + ADEGlobal.STRING_DELIMITER)) {
        task = tsk;
        break;
      }        
    }
    if (task != null) {
      taskQueue.remove(task);                                             
      if (params.get(1).equals("0"))
        ADETools.setTaskStatus(task, TaskStatus.COMPLETED,  null,    null);
      else if (params.get(1).equals(Integer.toString(ErrorStatus.PROCESS_KILLED.ordinal())))
        ADETools.setTaskStatus(task, TaskStatus.KILLED,  null,    null);
      else                                                                                     // exitCode
        ADETools.setTaskStatus(task, TaskStatus.FAILED,   "Execution failed, error code: " + params.get(1),    null);
    }
    
    return "";
  }
  
  /**
   * Method returns an array of results produced by a given query. At least two 
   * parameters are required (projectName and queryName) all other parameters are 
   * passed to the query as query parameters.
   */
  public String queryResult(ArrayList<String> params) {
    String result = "";
    if (params.size() < 2) return "";
    
    String [] queryParams = new String[params.size()-2];
    for (int i = 2; i < params.size(); i++) {
      queryParams[i-2] = params.get(i);
    }
    return DataAnalyser.getQueryResultTableAsString(params.get(0), params.get(1), queryParams);
  }
  
  
  private String getServerRunningTime() {
    long seconds = (new Date().getTime() - timeStarted)/1000;
    int day = (int)TimeUnit.SECONDS.toDays(seconds);        
    long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
    long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
    long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
    
    String result = second + " sec.";
    if (minute > 0)
      result = minute + " min" + ", " + result;
    if (hours > 0)
      result = hours + " h" + ", " + result;
    if (day > 0)
      result = day + " day(s)" + ", " + result;
    
    return result;
  }
  
  private String listTasks() {
    StringBuffer sb = new StringBuffer();
    for (ADETask tsk : taskQueue) {
      sb.append((sb.length() > 0 ? "\n" : "")).append(tsk.toStringPlus());
    }
    return sb.toString();
  }
  
  private String serverStatus() {
    int q=0, e=0;
    for (ADETask aDETask : taskQueue) {
      if (aDETask.getTaskStatus().equals(TaskStatus.QUEUED))    q++;
      if (aDETask.getTaskStatus().equals(TaskStatus.RUNNING)) e++;
    }
    
    return String.format("Server on for %s, Queued tasks: %d, Running tasks: %s\n", getServerRunningTime(), q,e);
  }
  
  private String processRequest(String request) {
    if (!request.toUpperCase().startsWith(ADEGlobal.REQ_GET_NEXT_TASK.toString().toUpperCase()))
      ADELog.log("REQUEST: " + request);
    
    String [] parts = request.split(" ");
    if (parts.length == 0) return "";
    
    parts[0] = parts[0].toUpperCase(); // request command
    
    // request parameters
    ArrayList<String> params = new ArrayList<>();
    for (int i = 1; i < parts.length; i++) 
      params.add(parts[i]);
    
    switch (parts[0]) {
        
      // return my ID (ID of caller; taskClient's ID); no parameters
      case ADEGlobal.REQ_WHO: 
        return Integer.toString(ID);
        
      // return the list of all tasks in the queue; no parameters  
      case ADEGlobal.REQ_LIST:
        return listTasks();
        
      // verifying server presence; no parameters
      case ADEGlobal.REQ_CHECK_Q:
        return ADEGlobal.REQ_CHECK_A;
        
      // prints server status; no parameters  
      case ADEGlobal.REQ_STATUS:
        return serverStatus();
        
      // appends task to the queue; parameters required: project algorithm testset mtype
      case ADEGlobal.REQ_ADD_TASK:
        return addTask(params);
        
      // prints the status of given task; parameters required: taskID
      case ADEGlobal.REQ_TASK_STATUS:
        return taskStatus(params);
        
      // returns task (project algorithm testset mtype); paramaters: computerID  
      case ADEGlobal.REQ_GET_NEXT_TASK:        
        return getNextTask(params);
        
      // removes task from the queue; parameters: taskID  
      case ADEGlobal.REQ_COMPLETE_TASK:
        return completeTask(params);
        
      // return JSON array with the status of tasks; parameters: 1, 2, 3, or 4 parameters are excepted.
      // if only one parameter is given, all tasks of a given project are checked; if 2 parameters are given, 
      // all tasks of a given project and algorithm are given; ...
      case ADEGlobal.REQ_PROJ_STATUS:
        return projectStatus(params);
        
      case ADEGlobal.REQ_QUERY_RES:
        return queryResult(params);                      
        
      default:
        return ADEGlobal.getErrorString("Unknown request");
    }
  }

  @Override
  public void run() {
    try (Scanner     sc = new Scanner(connection.getInputStream());
         PrintWriter pw = new PrintWriter(connection.getOutputStream());) {
        while (sc.hasNextLine()) {
          String request = sc.nextLine();
          if (request.equals("Bye")) {
            pw.println("Byebye.");
            break;
          }
          
          String answer  =  processRequest(request).replaceAll("\n", "<br>");
          pw.println(answer);
          pw.flush();
          
          // some requests finish the communication imediately
          if (request.startsWith(ADEGlobal.REQ_STATUS) ||
              request.startsWith(ADEGlobal.REQ_CHECK_Q)) {
            pw.close(); 
            return;
          }
        }                
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
       connection.close();
      } catch (Exception e) {
       ADELog.log(ADEGlobal.ERROR_PREFIX + e.toString());
      }
    }
  }

  static long timeStarted;
  public static void runServer() {
    int count = 0;
    try {
      ServerSocket socket1 = new ServerSocket(ADEGlobal.ADEPort);

      ADELog.log("TaskServer Initialized");
      
      timeStarted = new java.util.Date().getTime();
      while (true) {
        Socket connection = socket1.accept();
        Runnable runnable = new ADETaskServer(connection, ++count);
        Thread thread = new Thread(runnable);
        thread.start();
      }
    }
    catch (Exception e) { 
      ADELog.log(ADEGlobal.ERROR_PREFIX + e.toString());
    }
  }
  
}