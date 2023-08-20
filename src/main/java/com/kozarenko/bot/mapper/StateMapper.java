package com.kozarenko.bot.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozarenko.bot.model.State;

import java.util.List;
import java.util.Map;

public class StateMapper {

  private final ObjectMapper objectMapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public List<State> statesFromJson(String json) throws JsonProcessingException {
    Map<String, Object> jsonMap = objectMapper.readValue(json, new TypeReference<>() {});
    return objectMapper.convertValue(jsonMap.get("states"), new TypeReference<>() {});
  }
}
