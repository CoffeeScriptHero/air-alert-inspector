package com.kozarenko.bot.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kozarenko.bot.dto.StateDto;
import com.kozarenko.bot.dto.StatesDto;
import com.kozarenko.bot.model.State;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateMapper extends Mapper {

  public List<State> statesFromJson(String json) throws JsonProcessingException {
    return getObjectMapper().readValue(json, StatesDto.class).getStates();
  }

  public State stateFromJson(String json) throws JsonProcessingException {
    return getObjectMapper().readValue(json, StateDto.class).getState();
  }
}
