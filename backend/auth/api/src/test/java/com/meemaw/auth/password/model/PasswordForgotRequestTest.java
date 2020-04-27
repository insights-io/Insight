package com.meemaw.auth.password.model;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class PasswordForgotRequestTest {

  @Test
  public void dtoJacksonSerialization() throws JsonProcessingException {
    PasswordForgotRequestDTO passwordForgotRequestDTO = new PasswordForgotRequestDTO(
        "test@gmail.com");

    String payload = JacksonMapper.get().writeValueAsString(passwordForgotRequestDTO);
    assertThat(payload, sameJson("{\"email\":\"test@gmail.com\"}"));

    PasswordForgotRequestDTO deserialized = JacksonMapper.get()
        .readValue(payload, PasswordForgotRequestDTO.class);
    assertEquals(passwordForgotRequestDTO, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }


}
