package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

  @EntityGraph(attributePaths = {"interestTypes", "places"})
  Optional<Course> findById(UUID id);

}
