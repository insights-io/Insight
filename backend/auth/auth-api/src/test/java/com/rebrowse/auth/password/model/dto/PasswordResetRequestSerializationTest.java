package com.rebrowse.auth.password.model.dto;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class PasswordResetRequestSerializationTest {

  @Test
  public void jackson__should_correctly_serialize_password_reset_request_dto()
      throws JsonProcessingException {
    PasswordResetRequestDTO signUpCompleteRequest = new PasswordResetRequestDTO("superPassword");

    String payload = JacksonMapper.get().writeValueAsString(signUpCompleteRequest);
    MatcherAssert.assertThat(payload, sameJson("{\"password\":\"superPassword\"}"));

    PasswordResetRequestDTO deserialized =
        JacksonMapper.get().readValue(payload, PasswordResetRequestDTO.class);
    assertEquals(signUpCompleteRequest, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
