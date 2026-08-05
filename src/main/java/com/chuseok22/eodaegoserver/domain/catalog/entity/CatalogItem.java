package com.chuseok22.eodaegoserver.domain.catalog.entity;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
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
            columnNames = {"category", "sequence_number"})
    }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_id", nullable = false, foreignKey = @ForeignKey(name = "fk_catalog_item_source"))
  private CatalogSource source;

  @Column(nullable = false)
  private int sequenceNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogCategory category;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String childDescription;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogItemStatus status;

  private String nameOverride;

  private String imageUrlOverride;

  @Column(columnDefinition = "TEXT")
  private String featureOverride;

  private Double latitudeOverride;

  private Double longitudeOverride;

  // override는 저장 전에 정규화되어 null(원복) / ""(비움) / 실제값 세 가지만 들어온다.
  // 이름은 비울 수 없는 값이라 ""가 저장되지 않으므로 null 여부만 보면 된다.
  public String getName() {
    return nameOverride != null ? nameOverride : source.getName();
  }

  // 이미지는 "없음"도 유효한 상태다. null(원복)과 빈 문자열(이미지 없음으로 고정)을 구분한다.
  public String getImageUrl() {
    if (imageUrlOverride == null) {
      return source.getImageUrl();
    }
    return imageUrlOverride.isEmpty() ? null : imageUrlOverride;
  }

  public String getFeature() {
    return featureOverride != null ? featureOverride : Objects.requireNonNullElse(source.getDescription(), "");
  }

  public Double getLatitude() {
    return latitudeOverride != null ? latitudeOverride : source.getLatitude();
  }

  public Double getLongitude() {
    return longitudeOverride != null ? longitudeOverride : source.getLongitude();
  }

  public Long getExternalId() {
    return source.getExternalId();
  }

  public void update(
      String nameOverride, String featureOverride, String childDescription, CatalogItemStatus status,
      String imageUrlOverride, Double latitudeOverride, Double longitudeOverride) {
    this.nameOverride = nameOverride;
    this.featureOverride = featureOverride;
    this.childDescription = childDescription;
    this.status = status;
    this.imageUrlOverride = imageUrlOverride;
    this.latitudeOverride = latitudeOverride;
    this.longitudeOverride = longitudeOverride;
  }

  public void updateStatus(CatalogItemStatus status) {
    this.status = status;
  }
}
