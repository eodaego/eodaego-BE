package com.chuseok22.eodaegoserver.domain.course.entity;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlace extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  private int visitOrder;

  private Long facilityId;

  private String name;

  @Enumerated(EnumType.STRING)
  private CatalogCategory category;

  private Double latitude;

  private Double longitude;

  private Double mapX;

  private Double mapY;

}
