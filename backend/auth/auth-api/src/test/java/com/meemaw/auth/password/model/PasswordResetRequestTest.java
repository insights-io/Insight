package com.meemaw.auth.password.model;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PasswordResetRequestTest {

  @Test
  public void dtoJacksonSerialization() throws JsonProcessingException {
    UUID token = UUID.fromString("bc2a1cc5-62ed-45a2-b7a6-70520dadc33b");

    PasswordResetRequestDTO signupCompleteRequest =
        new PasswordResetRequestDTO("test@gmail.com", "ORG", token, "superPassword");

    String payload = JacksonMapper.get().writeValueAsString(signupCompleteRequest);
    assertThat(
        payload,
        sameJson(
            "{\"token\":\"bc2a1cc5-62ed-45a2-b7a6-70520dadc33b\",\"email\":\"test@gmail.com\",\"org\":\"ORG\",\"password\":\"superPassword\"}"));

    PasswordResetRequestDTO deserialized =
        JacksonMapper.get().readValue(payload, PasswordResetRequestDTO.class);
    assertEquals(signupCompleteRequest, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
