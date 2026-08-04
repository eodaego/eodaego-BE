package com.chuseok22.eodaegoserver.domain.course;

import org.springframework.data.domain.Sort;

public enum CourseFavoriteSortType {

  LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),

  OLDEST(Sort.by(Sort.Direction.ASC, "createdAt")),


  DURATION_SHORT(Sort.by(Sort.Direction.ASC, "course.estimatedDurationMinutes")
      .and(Sort.by(Sort.Direction.DESC, "createdAt"))),

  DURATION_LONG(Sort.by(Sort.Direction.DESC, "course.estimatedDurationMinutes")
      .and(Sort.by(Sort.Direction.DESC, "createdAt")));

  private final Sort sort;

  CourseFavoriteSortType(Sort sort) {
    this.sort = sort;
  }

  public Sort getSort() {
    return sort;
  }
}
