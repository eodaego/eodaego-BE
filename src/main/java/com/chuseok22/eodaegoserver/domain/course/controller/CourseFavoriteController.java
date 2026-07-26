package com.chuseok22.eodaegoserver.domain.course.controller;

import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteItemResponse;
import com.chuseok22.eodaegoserver.domain.course.dto.response.CourseFavoriteResponse;
import com.chuseok22.eodaegoserver.domain.course.service.CourseFavoriteService;
import com.chuseok22.logging.annotation.LogMonitoring;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class CourseFavoriteController implements CourseFavoriteControllerDocs {

  private final CourseFavoriteService courseFavoriteService;


  @Override
  @LogMonitoring
  @PostMapping(path = "/{courseId}", version = "1")
  public ResponseEntity<CourseFavoriteResponse> addFavorite(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID courseId) {
    return ResponseEntity.ok(courseFavoriteService.addFavorite(memberId, courseId));
  }

  @Override
  @LogMonitoring
  @GetMapping(path = "", version = "1")
  public ResponseEntity<List<CourseFavoriteItemResponse>> getFavorites(
      @AuthenticationPrincipal UUID memberId) {
    return ResponseEntity.ok(courseFavoriteService.getFavorites(memberId));
  }

  @Override
  @LogMonitoring
  @DeleteMapping(path = "/{courseId}", version = "1")
  public ResponseEntity<Void> deleteFavorite(
      @AuthenticationPrincipal UUID memberId,
      @PathVariable UUID courseId) {
    courseFavoriteService.deleteFavorite(memberId, courseId);
    return ResponseEntity.noContent().build();
  }
}
