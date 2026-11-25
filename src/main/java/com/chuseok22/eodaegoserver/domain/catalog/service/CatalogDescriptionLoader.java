package com.chuseok22.eodaegoserver.domain.catalog.service;

import com.chuseok22.eodaegoserver.domain.catalog.CatalogCategory;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class CatalogDescriptionLoader {

  private static final String DESCRIPTIONS_PATH = "catalog/catalog-descriptions.json";
  private static final String CHILD_DESCRIPTIONS_PATH = "catalog/catalog-child-descriptions.json";

  private final List<CatalogDescriptionEntry> entries;
  private final Map<CatalogCategory, Map<Long, CatalogDescriptionEntry>> entriesByCategory;

  // 파일은 jar에 포함되는 고정 리소스라 실행 중에는 바뀌지 않으므로 기동 시 한 번만 읽는다.
  // 형식 오류나 키 중복은 잘못된 기본값이 조용히 반영되지 않도록 기동 단계에서 바로 실패시킨다.
  // 일반 설명(feature)과 어린이 설명(childDescription)은 채우는 대상과 주기가 달라 파일을 나눠 두고,
  // 같은 키(category + externalId)를 기준으로 하나의 항목으로 합친다.
  public CatalogDescriptionLoader(ObjectMapper objectMapper) {
    this.entries = mergeEntries(
        loadEntries(objectMapper, DESCRIPTIONS_PATH),
        loadEntries(objectMapper, CHILD_DESCRIPTIONS_PATH));
    this.entriesByCategory = indexEntries(entries);
  }

  public List<CatalogDescriptionEntry> getEntries() {
    return entries;
  }

  public Optional<CatalogDescriptionEntry> find(CatalogCategory category, Long externalId) {
    return Optional.ofNullable(entriesByCategory.getOrDefault(category, Map.of()).get(externalId));
  }

  private List<CatalogDescriptionEntry> loadEntries(ObjectMapper objectMapper, String path) {
    ClassPathResource resource = new ClassPathResource(path);

    try (InputStream inputStream = resource.getInputStream()) {
      List<CatalogDescriptionEntry> loadedEntries =
          objectMapper.readValue(inputStream, new TypeReference<List<CatalogDescriptionEntry>>() {});
      return loadedEntries.stream().map(this::normalize).toList();
    } catch (IOException | JacksonException exception) {
      throw new IllegalStateException("도감 설명 파일을 읽을 수 없습니다: " + path, exception);
    }
  }

  // 두 파일은 서로를 보완하는 관계다. 한쪽에만 있는 항목은 그대로 두고, 양쪽에 있는 항목은
  // feature와 childDescription을 합친다. 같은 파일 안의 키 중복은 어느 쪽 값이 쓰일지 알 수 없으므로 실패시킨다.
  private List<CatalogDescriptionEntry> mergeEntries(
      List<CatalogDescriptionEntry> featureEntries, List<CatalogDescriptionEntry> childEntries) {
    Map<String, CatalogDescriptionEntry> merged = new LinkedHashMap<>();

    for (CatalogDescriptionEntry entry : featureEntries) {
      if (merged.putIfAbsent(keyOf(entry), entry) != null) {
        throw duplicateEntry(DESCRIPTIONS_PATH, entry);
      }
    }

    for (CatalogDescriptionEntry entry : childEntries) {
      CatalogDescriptionEntry existing = merged.get(keyOf(entry));
      if (existing == null) {
        merged.put(keyOf(entry), entry);
        continue;
      }
      if (existing.childDescription() != null) {
        throw duplicateEntry(CHILD_DESCRIPTIONS_PATH, entry);
      }
      merged.put(keyOf(entry), new CatalogDescriptionEntry(
          existing.category(),
          existing.externalId(),
          existing.name() != null ? existing.name() : entry.name(),
          existing.feature(),
          entry.childDescription()
      ));
    }

    return List.copyOf(merged.values());
  }

  private String keyOf(CatalogDescriptionEntry entry) {
    return entry.category() + ":" + entry.externalId();
  }

  private IllegalStateException duplicateEntry(String path, CatalogDescriptionEntry entry) {
    return new IllegalStateException("도감 설명 파일에 중복된 항목이 있습니다. file=" + path
        + ", category=" + entry.category() + ", externalId=" + entry.externalId());
  }

  private CatalogDescriptionEntry normalize(CatalogDescriptionEntry entry) {
    if (entry.category() == null || entry.externalId() == null) {
      throw new IllegalStateException("도감 설명 파일에 category 또는 externalId가 없는 항목이 있습니다: " + entry);
    }
    return new CatalogDescriptionEntry(
        entry.category(),
        entry.externalId(),
        entry.name(),
        blankToNull(entry.feature()),
        blankToNull(entry.childDescription())
    );
  }

  private Map<CatalogCategory, Map<Long, CatalogDescriptionEntry>> indexEntries(List<CatalogDescriptionEntry> entries) {
    Map<CatalogCategory, Map<Long, CatalogDescriptionEntry>> index = new EnumMap<>(CatalogCategory.class);

    for (CatalogDescriptionEntry entry : entries) {
      Map<Long, CatalogDescriptionEntry> entriesByExternalId =
          index.computeIfAbsent(entry.category(), category -> new HashMap<>());
      if (entriesByExternalId.putIfAbsent(entry.externalId(), entry) != null) {
        throw new IllegalStateException("도감 설명 파일에 중복된 항목이 있습니다. category="
            + entry.category() + ", externalId=" + entry.externalId());
      }
    }

    return index;
  }

  private String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value.strip();
  }
}
