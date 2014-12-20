package si.fri.algotest.global;



/**
 *
 * @author tomaz
 */
public enum ErrorStatus {

  STATUS_OK {
    public String toString() {
      return "OK";
    }
  },

  ERROR_CANT_COPY {
    public String toString() {
      return "Error: Can not copy file(s)";
    }
  },
  
  ERROR_CANT_COMPILE         {
    public String toString() {
      return "Error: Can not compile file(s)";
    }
  },
  
  ERROR_CANT_RUN             {
    public String toString() {
      return "Error: Can not run file(s)";
    }
  },

    
  ERROR_CANT_PERFORM_TEST             {
    public String toString() {
      return "Error: Can not perform this test.";
    }
  },
  
  
  ERROR_CANT_CREATEDIR       {
    public String toString() {
      return "Error: Can not create directory";
    }
  },
  
  ERROR_CANT_READFILE       {
    public String toString() {
      return "Error: Can not read a file";
    }
  },
  
  ERROR_CONVERT_FIELD       {
    public String toString() {
      return "Error: Can not convert the value of a field";
    }
  },
  
  ERROR_NOT_A_STRING_ARRAY       {
    public String toString() {
      return "Warning: The filed is not a String array.";
    }
  },
  
  ERROR_NOT_A_RESULTPARAMETER_ARRAY       {
    public String toString() {
      return "Error: The filed is not a ResultParameter array.";
    }
  },
  
  ERROR_CANT_DELETEDIR {
    public String toString() {
      return "Error: Can't delete directory.";
    }
  },
  
  ERROR_SOURCES_DONT_EXIST {
    public String toString() {
      return "Error: Source files don't exist.";
    }
  },
  
  ERROR_INVALID_DESTPATH {
    public String toString() {
      return "Error: Invalid destination path.";
    }
  },
  
  ERROR_INVALID_PROJECT {
    public String toString() {
      return "Error: Invalid project.";
    }
  },
  
  ERROR_INVALID_ALGORITHM {
    public String toString() {
      return "Error: Invalid algorithm.";
    }
  },

  ERROR_INVALID_TESTSET {
    public String toString() {
      return "Error: Invalid test set.";
    }
  },

  ERROR_INVALID_RESULTDESCRIPTION {
    public String toString() {
      return "Warning: Invalid result description file.";
    }
  },

  ERROR_CANT_INIT_FROM_JSON {
    public String toString() {
      return "Error: Invalid JSON format.";
    }
  },

  ERROR_CANT_WRITE_ALGORITHM_TO_FILE {
    public String toString() {
      return "Error: Algorithm serialization error";
    }
  },
  
  
  ERROR_CANT_READ_ALGORITHM_FROM_FILE {
    public String toString() {
      return "Error: Algorithm deserialization error.";
    }
  },

  ERROR_ALGORITHM_CANT_BE_CREATED {
    public String toString() {
      return "Error: Algorithm can not be created." ;
    }
  },  
  
  ERROR_PROCESS_CANT_BE_CREATED {
    public String toString() {
      return "Error: Process can not be created." ;
    }
  },
  
  MESSAGE_PROCESS_KILLED {
    public String toString() {
      return "Msg: Process was killed.";
    }
  },
  
  ERROR       {
    public String toString() {
      return "Error: Undefined error";
    }
  };


  
  private static String      lastErrorMerrage = "";
  private static ErrorStatus lastErrorStatus  = ErrorStatus.STATUS_OK;
  
  public static void resetErrorStatus() {
    setLastErrorMessage(ErrorStatus.STATUS_OK, "/");
  }
  
  public static ErrorStatus setLastErrorMessage(ErrorStatus status, String msg) {
    lastErrorMerrage = msg;
    lastErrorStatus  = status;
    
    if (msg.isEmpty())
      return status;
    
    if (status.isOK())
      ATLog.log(msg);
    else {      
      String where = String.format(" (Class: %s, Method: %s, line: %d)", 
	      Thread.currentThread().getStackTrace()[2].getClassName(), 
	      Thread.currentThread().getStackTrace()[2].getMethodName(),
	      Thread.currentThread().getStackTrace()[2].getLineNumber());   
      ATLog.log(status + (msg.isEmpty() ? "" : " -- ") + msg + where);
    }
    
    return status;
  }
  
  public static ErrorStatus getLastErrorStatus() {
    return lastErrorStatus;
  }
  public static String getLastErrorMessage() {
    return lastErrorStatus.toString() + ": " + lastErrorMerrage;
  }
  
  public boolean isOK() {
    return this.equals(STATUS_OK);
  }

}
