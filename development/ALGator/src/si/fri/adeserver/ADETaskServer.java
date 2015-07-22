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
  public String addTask(String request) {
    String [] parts = request.split(ADEGlobal.STRING_DELIMITER);
    if (parts.length != 5)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String project = parts[1], algorithm = parts[2], testset = parts[3], mType=parts[4];
    ADETask task = new ADETask(project, algorithm, testset, mType); 
    
    if (task != null) {
      taskQueue.add(task);
    
      ADETools.setTaskStatus(task, TaskStatus.QUEUED, null, null);
      Integer taskID = task.getField(ADETask.ID_TaskID);
      
      return taskID.toString();
    } else
      ADELog.log(ADEGlobal.ERROR_PREFIX + request);
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_ERROR_CREATE_TASK);
  }

  
  /**
   * Returns the next task that can be executed on cid or null if none exists
   */
  private ADETask getNextTaskFor(String cid) {      
      ADETask task = null;

    // currently every computer can execute every task
    // TODO: add logic for proper computer-task matching      
    for (ADETask aDETask : taskQueue) {
      if (aDETask.getTaskStatus().equals(TaskStatus.QUEUED)) {
        task = aDETask; break;
      }
    }            
      
    return task;
  }
  
  /**
   * Finds the next task that can be executed on a computer with cid          <br>
   * Call: getNextTask cid                                                    <br>
   * Return: NO_TASKS or nextTask (id proj alg testset mtype)
   */
  private String getNextTask(String request) {
    String [] parts = request.split(ADEGlobal.STRING_DELIMITER);
    if (parts.length != 2)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String cid = parts[1];
    ADETask task = getNextTaskFor(cid);
    if (task != null) {
        ADETools.setTaskStatus(task, TaskStatus.RUNNING, null, cid);
        return task.toString();
    } else
    return ADEGlobal.NO_TASKS;
  }
  
  /**
   * Removes task from tasksProcessed list and writes status to the file
   * Call: completeTask tid
   * Return: "" or error message
   */
  private String completeTask(String request) {
    String [] parts = request.split(ADEGlobal.STRING_DELIMITER);
    if (parts.length != 2)
      return ADEGlobal.getErrorString(ADEGlobal.ERROR_INVALID_NPARS);
    
    String tid = parts[1];
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
      ADETools.setTaskStatus(task, TaskStatus.COMPLETED, null, null);
    }
    
    return "";
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
  
  private String serverStatus() {
    int q=0, e=0;
    for (ADETask aDETask : taskQueue) {
      if (aDETask.getTaskStatus().equals(TaskStatus.QUEUED))    q++;
      if (aDETask.getTaskStatus().equals(TaskStatus.RUNNING)) e++;
    }
    
    return String.format("Server on for %s, Queued tasks: %d, Running tasks: %s\n", getServerRunningTime(), q,e);
  }
  
  private String processRequest(String request) {
    String [] parts = request.split(" ");
    if (parts.length == 0) return "";
    
    switch (parts[0]) {
        
      case "WHO": 
        return Integer.toString(ID);
      case "LIST":
          StringBuffer sb = new StringBuffer();
        for (ADETask tsk : taskQueue) {
          sb.append((sb.length() > 0 ? "\n" : "")).append(tsk.toStringPlus());
        }
        return sb.toString();

        
      // verifying server presence
      case ADEGlobal.REQ_CHECK_Q:
        return ADEGlobal.REQ_CHECK_A;
        
      case ADEGlobal.REQ_STATUS:
        return serverStatus();
        
      case ADEGlobal.REQ_ADD_TASK:
        return addTask(request);
      case ADEGlobal.REQ_GET_NEXT_TASK:
        return getNextTask(request);
      case ADEGlobal.REQ_COMPLETE_TASK:
        return completeTask(request);
        
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
          
          String answer  =  processRequest(request);
          pw.println(answer);
          pw.flush();
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
