package com.chuseok22.eodaegoserver.domain.quiz.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import com.chuseok22.eodaegoserver.domain.quiz.dto.external.AiPhotoRecognitionResponse;
import com.chuseok22.eodaegoserver.global.exception.CustomException;
import com.chuseok22.eodaegoserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizAiClient {

  private static final String RECOGNITION_URI = "/api/v1/recognition/identify";

  private final RestClient aiServerRestClient;

  public AiPhotoRecognitionResponse identify(CatalogCategory catalogType, MultipartFile image) {
    MediaType parsedType = MediaType.parseMediaType(image.getContentType());
    String filename = StringUtils.hasText(image.getOriginalFilename()) ? image.getOriginalFilename() : "image";

    HttpHeaders imageHeaders = new HttpHeaders();
    imageHeaders.setContentType(new MediaType(parsedType.getType(), parsedType.getSubtype()));
    imageHeaders.setContentDisposition(ContentDisposition.formData().name("image").filename(filename).build());

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("catalog_type", catalogType.name());
    body.add("image", new HttpEntity<>(image.getResource(), imageHeaders));

    try {
      AiPhotoRecognitionResponse response = aiServerRestClient.post()
          .uri(RECOGNITION_URI)
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(body)
          .retrieve()
          .body(AiPhotoRecognitionResponse.class);

      if (response == null) {
        log.error("AI 서버 사진 인식 응답 본문이 비어 있습니다. uri={}, catalogType={}", RECOGNITION_URI, catalogType);
        throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
      }

      return response;
    } catch (HttpStatusCodeException exception) {
      log.error("AI 서버 사진 인식 응답 오류. uri={}, catalogType={}, status={}, body={}",
          RECOGNITION_URI, catalogType, exception.getStatusCode(), exception.getResponseBodyAsString());
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    } catch (RestClientException exception) {
      log.error("AI 서버 사진 인식 호출 실패. uri={}, catalogType={}", RECOGNITION_URI, catalogType, exception);
      throw new CustomException(ErrorCode.AI_SERVER_UNAVAILABLE);
    }
  }
}
