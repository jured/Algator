package si.fri.algotest.entities;

/**
 *
 * @author tomaz
 */
public enum ComputerCapability {
  UNKNOWN, AEE_EM, AEE_CNT, AEE_JVM, WEB;

  @Override
  public String toString() {
    switch (this) {
      case UNKNOWN:
        return "unknown";
      case AEE_EM:
        return "aee_em";
      case AEE_CNT:
        return "aee_cnt";
      case AEE_JVM:
        return "aee_jvm";
      case WEB:
        return "web";
      default:
        return "/unknown/";
    }
  }

  static ComputerCapability getComputerCapability(String cpbDesc) {
    for (ComputerCapability cpb : ComputerCapability.values()) {
      if (cpbDesc.toLowerCase().equals(cpb.toString().toLowerCase())) {
        return cpb;
      }
    }
    return UNKNOWN;
  }
}
