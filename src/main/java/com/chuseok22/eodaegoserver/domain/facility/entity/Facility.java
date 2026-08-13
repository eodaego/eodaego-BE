package com.chuseok22.eodaegoserver.domain.facility.entity;

import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private Long aiFacilityId;

  private String code;

  private String sourceCategory;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String intro;

  @Column(columnDefinition = "TEXT")
  private String description;

  private Double latitude;

  private Double longitude;

  private String facilityType;

  @Column(nullable = false)
  private LocalDateTime lastSeenAt;

  private LocalTime openTime;

  private LocalTime closeTime;

  private String operatingNote;

  public void updateFromExternal(
      String code, String sourceCategory, String name, String intro, String description,
      Double latitude, Double longitude, String facilityType) {
    this.code = code;
    this.sourceCategory = sourceCategory;
    this.name = name;
    this.intro = intro;
    this.description = description;
    this.latitude = latitude;
    this.longitude = longitude;
    this.facilityType = facilityType;
  }

  public void updateOperatingHours(LocalTime openTime, LocalTime closeTime, String operatingNote) {
    this.openTime = openTime;
    this.closeTime = closeTime;
    this.operatingNote = operatingNote;
  }
}
