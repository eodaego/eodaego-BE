package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.entity.Admin;
import com.chuseok22.eodaegoserver.domain.admin.repository.AdminRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

  private final AdminRepository adminRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Admin admin = adminRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 관리자입니다: " + username));

    return User.withUsername(admin.getUsername())
        .password(admin.getPassword())
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())))
        .build();
  }
}
