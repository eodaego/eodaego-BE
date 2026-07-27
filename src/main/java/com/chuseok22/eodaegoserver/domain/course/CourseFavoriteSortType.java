package com.chuseok22.eodaegoserver.domain.course;

public enum CourseFavoriteSortType {

  SAVED_AT("createdAt"),
  DURATION("course.durationMinutes");

  private final String property;

  CourseFavoriteSortType(String property) {
    this.property = property;
  }

  public String getProperty() {
    return property;
  }
}
