package TaskManagementSystem.model;

public enum Priority {
  LOW,
  MEDIUM,
  HIGH;

  public static Priority up(Priority priority) {
    if (priority == Priority.HIGH) {
      return Priority.HIGH;
    } else if (priority == Priority.MEDIUM) {
      return Priority.HIGH;
    } else {
      return Priority.MEDIUM;
    }
  }

  public static Priority down(Priority priority) {
    if (priority == Priority.LOW) {
      return Priority.LOW;
    } else if (priority == Priority.MEDIUM) {
      return Priority.LOW;
    } else {
      return Priority.MEDIUM;
    }
  }
}
