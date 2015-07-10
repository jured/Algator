package si.fri.adeserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author tomaz
 */
public class ADETools {
  
  public static ArrayList<ADETask> readADETasks() {
    ArrayList<ADETask> tasks = new ArrayList<>();
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

  public static void writeADETasks(ArrayList<ADETask> tasks) {
    File taskFile = new File(ADEGlobal.getADETasklistFilename());
    try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(taskFile));) {
      for (ADETask aDETask : tasks) {
        dos.writeUTF(aDETask.toJSONString());
      }
    } catch (Exception e) {
      // if error ocures, nothing can be done
    }
  }
  
  public static void writeTaskStatus(int idt, TaskStatus status, String msg) {
    String idtFilename = ADEGlobal.getTaskStatusFilename(idt);
    String startDate="", statusMsg = "", endDate="";
    if (status.equals(TaskStatus.CREATED)) {      
      startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    } else try (Scanner sc = new Scanner(new File(idtFilename))) {
        startDate = sc.nextLine();
        statusMsg = sc.nextLine();
        endDate   = sc.nextLine();
      } catch (Exception e) {
        // on error - use default values
      }
    if (msg != null)
      statusMsg = msg;
    if (status.equals(TaskStatus.COMPLETED))
      endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    
    try (PrintWriter pw = new PrintWriter(new File(idtFilename))) {
     pw.println(startDate);
     pw.println(statusMsg);
     pw.print(endDate);
    } catch (Exception e) {
      // nothing can be done if error occures - ignore
    }
  }
}
