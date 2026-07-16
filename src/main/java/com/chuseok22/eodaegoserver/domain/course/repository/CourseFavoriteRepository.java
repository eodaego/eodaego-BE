package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFavoriteRepository extends JpaRepository<CourseFavorite, UUID> {

  Optional<CourseFavorite> findByMemberIdAndCourseId(UUID memberId, UUID courseId);

  boolean existsByMemberIdAndCourseId(UUID memberId, UUID courseId);

  List<CourseFavorite> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
