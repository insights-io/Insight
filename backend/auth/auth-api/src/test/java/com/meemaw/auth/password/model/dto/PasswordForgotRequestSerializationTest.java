package com.meemaw.auth.password.model.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class PasswordForgotRequestSerializationTest {

  @Test
  public void jacksonSerialization() throws JsonProcessingException {
    PasswordForgotRequestDTO passwordForgotRequestDTO =
        new PasswordForgotRequestDTO("test@gmail.com");

    String payload = JacksonMapper.get().writeValueAsString(passwordForgotRequestDTO);
    assertThat(payload, sameJson("{\"email\":\"test@gmail.com\"}"));

    PasswordForgotRequestDTO deserialized =
        JacksonMapper.get().readValue(payload, PasswordForgotRequestDTO.class);
    assertEquals(passwordForgotRequestDTO, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
