package si.fri.adeserver;

/**
 *
 * @author tomaz
 */

public enum TaskStatus {
  UNKNOWN, QUEUED, RUNNING, COMPLETED;

  @Override
  public String toString() {
    switch (this) {
      case QUEUED:
        return "QUEUED";
      case RUNNING:
        return "RUNNING";
      case COMPLETED:
        return "COMPLETED";
    }
    return "/unknown/";
  }
  
  public static TaskStatus getTaskStatus(String status) {
    for (TaskStatus sts : TaskStatus.values()) {
      if (status.equals(sts.toString())) {
        return sts;
      }
    }
    return UNKNOWN;
  }
}
