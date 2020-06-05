package com.meemaw.auth.password.model.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class PasswordResetRequestSerializationTest {

  @Test
  public void jacksonSerialization() throws JsonProcessingException {
    PasswordResetRequestDTO signUpCompleteRequest = new PasswordResetRequestDTO("superPassword");

    String payload = JacksonMapper.get().writeValueAsString(signUpCompleteRequest);
    assertThat(payload, sameJson("{\"password\":\"superPassword\"}"));

    PasswordResetRequestDTO deserialized =
        JacksonMapper.get().readValue(payload, PasswordResetRequestDTO.class);
    assertEquals(signUpCompleteRequest, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
