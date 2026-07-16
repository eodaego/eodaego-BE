package com.chuseok22.eodaegoserver.domain.course.entity;

import com.chuseok22.eodaegoserver.domain.course.EntranceGate;
import com.chuseok22.eodaegoserver.domain.course.InterestType;
import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;


  @Builder.Default
  @ElementCollection
  @CollectionTable(name = "course_interest_type", joinColumns = @JoinColumn(name = "course_id"))
  @Enumerated(EnumType.STRING)
  @BatchSize(size = 20)
  private List<InterestType> interestTypes = new ArrayList<>();

  private String tagLabel;

  private int durationMinutes;

  @Enumerated(EnumType.STRING)
  private EntranceGate entrance;

  @Enumerated(EnumType.STRING)
  private EntranceGate exit;

  @Builder.Default
  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
  @OrderBy("visitOrder asc")
  @BatchSize(size = 20)
  private List<CoursePlace> places = new ArrayList<>();

}
