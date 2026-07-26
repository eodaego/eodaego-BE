package com.chuseok22.eodaegoserver.global.exception;

import org.hibernate.exception.ConstraintViolationException;

public final class ConstraintViolationInspector {

  private ConstraintViolationInspector() {
  }

  public static boolean matches(Throwable throwable, String expectedConstraintName) {
    Throwable current = throwable;

    while (current != null) {
      if (current instanceof ConstraintViolationException exception) {
        return expectedConstraintName.equalsIgnoreCase(exception.getConstraintName());
      }
      current = current.getCause();
    }

    return false;
  }
}
