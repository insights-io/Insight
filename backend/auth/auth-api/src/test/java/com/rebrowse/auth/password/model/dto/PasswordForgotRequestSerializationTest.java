package com.rebrowse.auth.password.model.dto;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import com.rebrowse.test.utils.GlobalTestData;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class PasswordForgotRequestSerializationTest {

  @Test
  public void jackson__should_correctly_serialize_password_forgot_request_dto()
      throws JsonProcessingException {
    PasswordForgotRequestDTO passwordForgotRequestDTO =
        new PasswordForgotRequestDTO("test@gmail.com", GlobalTestData.LOCALHOST_REDIRECT_URL);

    String payload = JacksonMapper.get().writeValueAsString(passwordForgotRequestDTO);
    MatcherAssert.assertThat(
        payload,
        sameJson("{\"email\":\"test@gmail.com\", \"redirect\": \"http://localhost:3000/test\"}"));

    PasswordForgotRequestDTO deserialized =
        JacksonMapper.get().readValue(payload, PasswordForgotRequestDTO.class);
    assertEquals(passwordForgotRequestDTO, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
