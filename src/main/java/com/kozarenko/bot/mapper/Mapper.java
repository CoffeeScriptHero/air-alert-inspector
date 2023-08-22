package com.kozarenko.bot.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class Mapper {

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .registerModule(new JavaTimeModule());

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
