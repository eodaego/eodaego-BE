package com.chuseok22.eodaegoserver.domain.catalog.entity;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.catalog.CatalogItemStatus;
import com.chuseok22.eodaegoserver.domain.facility.entity.Facility;
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
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@Entity
// 설명 기본값만 갱신할 때 관리자 override 컬럼까지 스테일 값으로 덮어쓰지 않도록 변경된 컬럼만 UPDATE한다.
@DynamicUpdate
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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_id", foreignKey = @ForeignKey(name = "fk_catalog_item_source"))
  private CatalogSource source;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id", foreignKey = @ForeignKey(name = "fk_catalog_item_facility"))
  private Facility facility;

  @Column(nullable = false)
  private int sequenceNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CatalogItemStatus status;

  private String nameOverride;

  private String imageUrlOverride;

  @Column(columnDefinition = "TEXT")
  private String featureOverride;

  @Column(columnDefinition = "TEXT")
  private String childDescriptionOverride;

  // 설명 파일(catalog/catalog-descriptions.json)에서 채워지는 기본값. 관리자 수정 대상이 아니다.
  @Column(columnDefinition = "TEXT")
  private String defaultFeature;

  @Column(columnDefinition = "TEXT")
  private String defaultChildDescription;

  private Double latitudeOverride;

  private Double longitudeOverride;

  public String getName() {
    if (nameOverride != null) {
      return nameOverride;
    }
    return facility != null ? facility.getName() : source.getName();
  }

  public String getImageUrl() {
    if (imageUrlOverride != null) {
      return imageUrlOverride;
    }
    return facility != null ? null : source.getImageUrl();
  }

  public String getFeature() {
    if (featureOverride != null) {
      return featureOverride;
    }
    if (defaultFeature != null) {
      return defaultFeature;
    }
    String description = facility != null ? facility.getDescription() : source.getDescription();
    return Objects.requireNonNullElse(description, "");
  }

  public String getChildDescription() {
    if (childDescriptionOverride != null) {
      return childDescriptionOverride;
    }
    return Objects.requireNonNullElse(defaultChildDescription, "");
  }

  public String getOriginalName() {
    return facility != null ? facility.getName() : source.getName();
  }

  public Double getLatitude() {
    if (latitudeOverride != null) {
      return latitudeOverride;
    }
    return facility != null ? facility.getLatitude() : null;
  }

  public Double getLongitude() {
    if (longitudeOverride != null) {
      return longitudeOverride;
    }
    return facility != null ? facility.getLongitude() : null;
  }

  public Long getExternalId() {
    return facility != null ? facility.getAiFacilityId() : source.getExternalId();
  }

  public void update(
      String nameOverride, String featureOverride, String childDescriptionOverride, CatalogItemStatus status,
      String imageUrlOverride, Double latitudeOverride, Double longitudeOverride) {
    this.nameOverride = nameOverride;
    this.featureOverride = featureOverride;
    this.childDescriptionOverride = childDescriptionOverride;
    this.status = status;
    this.imageUrlOverride = imageUrlOverride;
    this.latitudeOverride = latitudeOverride;
    this.longitudeOverride = longitudeOverride;
  }

  public void updateStatus(CatalogItemStatus status) {
    this.status = status;
  }

  public void updateDefaultDescriptions(String defaultFeature, String defaultChildDescription) {
    this.defaultFeature = defaultFeature;
    this.defaultChildDescription = defaultChildDescription;
  }
}
