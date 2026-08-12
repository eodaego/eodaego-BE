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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogCategory category;

  @Column(nullable = false)
  private Long externalId;

  @Column(nullable = false)
  private String name;

  private String imageUrl;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private LocalDateTime lastSeenAt;

  public void updateFromExternal(String name, String imageUrl, String description) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.description = description;
  }
}
