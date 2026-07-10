package com.chuseok22.eodaegoserver.domain.member.entity;

import com.chuseok22.eodaegoserver.domain.member.DeviceType;
import com.chuseok22.eodaegoserver.domain.member.SocialType;
import com.chuseok22.eodaegoserver.global.entity.BaseEntity;
import com.chuseok22.eodaegoserver.global.security.Role;
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
@Builder
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_member_social_provider", columnNames = {"social_type", "provider_id"})
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SocialType socialType;

  @Column(nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private boolean firstLogin;

  @Column(nullable = false)
  private String deviceId;


  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeviceType deviceType;

  @Column
  private String fcmToken;

  @Column(nullable = false)
  private boolean privacyPolicyAgreed;

  @Column(nullable = false)
  private boolean locationInfoAgreed;

  @Column(nullable = false)
  private boolean termsOfServiceAgreed;

  @Column(nullable = false)
  private boolean marketingAgreed;

  private LocalDateTime termsAgreedAt;

  private LocalDateTime marketingAgreedAt;
}
