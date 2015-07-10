package si.fri.adeserver;

/**
 *
 * @author tomaz
 */

public enum TaskStatus {
  CREATED, SCHEDULED, PROCESSING, COMPLETED;

  @Override
  public String toString() {
    switch (this) {
      case CREATED:
        return "unknown";
      case SCHEDULED:
        return "aee_em";
      case PROCESSING:
        return "aee_cnt";
      case COMPLETED:
        return "aee_jvm";
      default:
        return "/unknown/";
    }
  }
}
