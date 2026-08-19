package com.chuseok22.eodaegoserver.domain.course.repository;

import com.chuseok22.eodaegoserver.domain.course.entity.CourseFavorite;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseFavoriteRepository extends JpaRepository<CourseFavorite, UUID> {

  Optional<CourseFavorite> findByMemberIdAndCourseId(UUID memberId, UUID courseId);

  boolean existsByMemberIdAndCourseId(UUID memberId, UUID courseId);

  @EntityGraph(attributePaths = "course")
  List<CourseFavorite> findByMemberId(UUID memberId, Sort sort);

  // 엔티티에 컬럼이 추가되면 이 SQL도 함께 갱신해야 한다
  // clearAutomatically가 없는 것은 의도된 것 — 호출부에 적재된 CourseFavorite이 없다.
  @Modifying
  @Query(value = """
      INSERT INTO course_favorite (id, member_id, course_id, created_at, updated_at)
      VALUES (gen_random_uuid(), :memberId, :courseId, :now, :now)
      ON CONFLICT (member_id, course_id) DO NOTHING
      """, nativeQuery = true)
  int insertIfAbsent(@Param("memberId") UUID memberId,
                     @Param("courseId") UUID courseId,
                     @Param("now") LocalDateTime now);

  // 엔티티를 읽어 delete()하면 동시 삭제 시 영향 행 0건으로 StaleStateException이 난다.
  @Modifying(clearAutomatically = true)
  @Query("delete from CourseFavorite cf where cf.member.id = :memberId and cf.course.id = :courseId")
  int deleteByMemberIdAndCourseId(@Param("memberId") UUID memberId, @Param("courseId") UUID courseId);
}
