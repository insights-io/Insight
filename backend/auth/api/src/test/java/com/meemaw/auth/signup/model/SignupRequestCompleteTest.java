package com.meemaw.auth.signup.model;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SignupRequestCompleteTest {

  @Test
  public void dtoJacksonSerialization() throws JsonProcessingException {
    UUID token = UUID.fromString("bc2a1cc5-62ed-45a2-b7a6-70520dadc33b");

    SignupRequestCompleteDTO signupCompleteRequest = new SignupRequestCompleteDTO(
        "test@gmail.com",
        "ORG",
        token,
        "superPassword");

    String payload = JacksonMapper.get().writeValueAsString(signupCompleteRequest);
    assertThat(payload, sameJson(
        "{\"token\":\"bc2a1cc5-62ed-45a2-b7a6-70520dadc33b\",\"email\":\"test@gmail.com\",\"password\":\"superPassword\",\"org\":\"ORG\"}"));

    SignupRequestCompleteDTO deserialized = JacksonMapper.get()
        .readValue(payload, SignupRequestCompleteDTO.class);
    assertEquals(signupCompleteRequest, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
