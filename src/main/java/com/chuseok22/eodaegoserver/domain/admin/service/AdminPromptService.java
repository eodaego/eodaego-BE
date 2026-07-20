package com.chuseok22.eodaegoserver.domain.admin.service;

import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateCreateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.request.PromptTemplateUpdateRequest;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AiModelListResponseView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.AiModelSummaryView;
import com.chuseok22.eodaegoserver.domain.admin.dto.response.PromptTemplateView;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPromptService {

  private static final String BASE_URI = "/api/v1/prompts";

  private final RestClient aiServerRestClient;

  public List<PromptTemplateView> findAll() {
    try {
      return aiServerRestClient.get()
          .uri(BASE_URI)
          .retrieve()
          .body(new ParameterizedTypeReference<List<PromptTemplateView>>() {});
    } catch (RestClientException e) {
      log.warn("[AdminPromptService] 프롬프트 템플릿 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  public PromptTemplateView findById(Integer promptId) {
    return findAll().stream()
        .filter(prompt -> prompt.id().equals(promptId))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_TEMPLATE_NOT_FOUND));
  }

  public PromptTemplateView create(PromptTemplateCreateRequest request) {
    boolean nameExists = findAll().stream()
        .anyMatch(prompt -> prompt.name().equals(request.name()));
    if (nameExists) {
      throw new CustomException(ErrorCode.PROMPT_TEMPLATE_NAME_ALREADY_EXISTS);
    }
    try {
      return aiServerRestClient.post()
          .uri(BASE_URI)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(PromptTemplateView.class);
    } catch (RestClientException e) {
      log.warn("[AdminPromptService] 프롬프트 템플릿 생성 실패: {}", e.getMessage());
      throw toCustomException(e);
    }
  }

  public PromptTemplateView update(Integer promptId, PromptTemplateUpdateRequest request) {
    try {
      return aiServerRestClient.patch()
          .uri(BASE_URI + "/{promptId}", promptId)
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(PromptTemplateView.class);
    } catch (RestClientException e) {
      log.warn("[AdminPromptService] 프롬프트 템플릿 수정 실패: promptId={}, message={}", promptId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public void delete(Integer promptId) {
    try {
      aiServerRestClient.delete()
          .uri(BASE_URI + "/{promptId}", promptId)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.warn("[AdminPromptService] 프롬프트 템플릿 삭제 실패: promptId={}, message={}", promptId, e.getMessage());
      throw toCustomException(e);
    }
  }

  public PromptTemplateView activate(Integer promptId) {
    PromptTemplateView current = findById(promptId);
    PromptTemplateUpdateRequest request = new PromptTemplateUpdateRequest(
        current.name(), current.model(), current.purpose(), current.templateText(), true);
    return update(promptId, request);
  }

  public List<String> listModels() {
    try {
      AiModelListResponseView response = aiServerRestClient.get()
          .uri("/api/v1/ai/models")
          .retrieve()
          .body(AiModelListResponseView.class);
      if (response == null || response.models() == null) {
        return List.of();
      }
      return response.models().stream()
          .map(AiModelSummaryView::name)
          .toList();
    } catch (RestClientException e) {
      log.warn("[AdminPromptService] AI 모델 목록 조회 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }

  private CustomException toCustomException(RestClientException e) {
    if (e instanceof RestClientResponseException responseException) {
      if (responseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
        return new CustomException(ErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
      }
      if (responseException.getStatusCode().is4xxClientError()) {
        return new CustomException(ErrorCode.INVALID_REQUEST);
      }
    }
    return new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
  }
}
