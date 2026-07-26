package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, UUID> {

  @EntityGraph(attributePaths = "places")
  @Query("select c from Course c where c.id = :courseId")
  Optional<Course> findByIdWithPlaces(@Param("courseId") UUID courseId);

}
