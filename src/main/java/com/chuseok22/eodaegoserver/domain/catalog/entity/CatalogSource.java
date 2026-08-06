package com.chuseok22.eodaegoserver.domain.catalog.entity;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_catalog_source_category_external_id",
        columnNames = {"category", "external_id"})
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogSource extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // AI 서버는 동물/식물/시설마다 id를 각자 1부터 매기므로 externalId 단독으로는 유일하지 않다.
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogCategory category;

  @Column(nullable = false)
  private Long externalId;

  @Column(nullable = false)
  private String name;

  private String imageUrl;

  private Double latitude;

  private Double longitude;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String intro;

  private String facilityType;

  @Column(nullable = false)
  private LocalDateTime lastSeenAt;

  public void updateFromExternal(
      String name, String imageUrl, Double latitude, Double longitude,
      String description, String intro, String facilityType) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
    this.description = description;
    this.intro = intro;
    this.facilityType = facilityType;
  }
}
