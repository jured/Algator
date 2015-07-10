package si.fri.adeserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author tomaz
 */
public class TaskServer implements Runnable {
  // one array of tasks is shared by all servers
  public static ArrayList<ADETask> tasks;
  
  private Socket connection;
  private int ID;  // server ID


  public TaskServer(Socket connection, int count) {
    // when a server is created, array of tasks is read from file
    if (tasks == null)
      tasks = ADETools.readADETasks();
    
    this.connection = connection;
    this.ID = count;
  }

  

  public ADETask addTask(String project, String algorithm, String testset, String mType) {     
    ADETask task = new ADETask(project, algorithm, testset, mType); 
    tasks.add(task);
    ADETools.writeADETasks(tasks);
    
    return task;
  }

  private String processRequest(String request) {
    String [] parts = request.split(" ");
    if (parts.length == 0) return "";
    
    switch (parts[0].toUpperCase()) {
      case "WHO": 
        return Integer.toString(ID);
      case "ADD":
        if (parts.length!=5)
          return "Invalid number of parameters";        
        ADETask task = addTask(parts[1], parts[2], parts[3], parts[4]);
        ADETools.writeTaskStatus(task.getFieldAsInt(ADETask.ID_TaskID), TaskStatus.CREATED, null);
        return "Task added: " + task.toString();
      case "LIST":
        StringBuffer sb = new StringBuffer();
        for (ADETask tsk : tasks) {
          sb.append((sb.length() > 0 ? "\n" : "") + tsk.toString());
        }
        return sb.toString();
      default:
        return "Unknown request";
    }
  }

  @Override
  public void run() {
    try (Scanner     sc = new Scanner(connection.getInputStream());
         PrintWriter pw = new PrintWriter(connection.getOutputStream());) {
        while (true) {
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
      } catch (Exception e) {}
    }
  }

  public static void main(String[] args) {
    int count = 0;
    try{
      ServerSocket socket1 = new ServerSocket(ADEGlobal.ADEPort);
      System.out.println("ADEServer Initialized");
      while (true) {
        Socket connection = socket1.accept();
        Runnable runnable = new TaskServer(connection, ++count);
        Thread thread = new Thread(runnable);
        thread.start();
      }
    }
    catch (Exception e) {}
  }
  
}
