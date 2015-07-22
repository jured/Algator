package si.fri.adeserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;
import static si.fri.adeserver.ADETaskServer.taskQueue;

/**
 *
 * @author tomaz
 */
public class ADETools {
  
  public static Vector<ADETask> readADETasks() {
    Vector<ADETask> tasks = new Vector<>();
    File taskFile = new File(ADEGlobal.getADETasklistFilename());
    if (taskFile.exists()) {
      try (DataInputStream dis = new DataInputStream(new FileInputStream(taskFile));) {
        while (dis.available() > 0) {
          String line = dis.readUTF();
          tasks.add(new ADETask(line));
        }
      } catch (Exception e) {
        // if error ocures, nothing can be done
      }
    }
    return tasks;
  }

  public static void writeADETasks(Vector<ADETask> tasks) {
    File taskFile = new File(ADEGlobal.getADETasklistFilename());
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(taskFile));) {
      for (ADETask aDETask : tasks) {
        dos.writeUTF(aDETask.toJSONString());
      }
    } catch (Exception e) {
      // if error ocures, nothing can be done
    }
  }


  
  /**
   * Sets the status of a task and writes this status to the task status file
   */
  public static void setTaskStatus(ADETask task, TaskStatus status, String msg, String computer) {
    task.setTaskStatus(status, computer);
    
    int idt = task.getFieldAsInt(ADETask.ID_TaskID);
    setTaskStatus(idt, status, msg);
    
    ADETools.writeADETasks(taskQueue);
    ADELog.log(status + " " + task.toString());
  }
  
  /**
   * Writes the status of a task to the task status file
   */
  public static void setTaskStatus(int idt, TaskStatus status, String msg) {
    String idtFilename = ADEGlobal.getTaskStatusFilename(idt);
    String startDate="", statusMsg = "", endDate="";
    if (status.equals(TaskStatus.QUEUED)) {      
      startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ": Task queued";
    } else try (Scanner sc = new Scanner(new File(idtFilename))) {
      startDate = sc.nextLine();
      statusMsg = sc.nextLine();
      endDate   = sc.nextLine();
    } catch (Exception e) {
      // on error - use default values
    }
    
    if (msg != null)
      statusMsg = msg;

    if (status.equals(TaskStatus.RUNNING)) {           
      statusMsg = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ": Task running" 
              + (statusMsg.isEmpty() ? "" : " - ") + statusMsg;
    }
  
    if (status.equals(TaskStatus.COMPLETED))
      endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ": Task completed";
    
    try (PrintWriter pw = new PrintWriter(new File(idtFilename))) {
     pw.println(startDate);
     pw.println(statusMsg);
     pw.print(endDate);
    } catch (Exception e) {
      // nothing can be done if error occures - ignore
    }
  }
}
