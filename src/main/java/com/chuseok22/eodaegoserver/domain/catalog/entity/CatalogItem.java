package com.chuseok22.eodaegoserver.domain.catalog.entity;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
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
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_catalog_item_category_sequence_number",
            columnNames = {"category", "sequence_number"}),
        @UniqueConstraint(
            name = "uk_catalog_item_category_external_id",
            columnNames = {"category", "external_id"})
    }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private int sequenceNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogCategory category;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String feature;

  @Column(nullable = false)
  private String childDescription;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogItemStatus status;

  private String imageUrl;

  private Double latitude;

  private Double longitude;

  private Long externalId;

  public void update(String name, String feature, String childDescription, CatalogItemStatus status, String imageUrl, Double latitude, Double longitude) {
    this.name = name;
    this.feature = feature;
    this.childDescription = childDescription;
    this.status = status;
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public void updateStatus(CatalogItemStatus status) {
    this.status = status;
  }

  public void updateSyncedFields(String name, String imageUrl, Double latitude, Double longitude) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
  }

}

