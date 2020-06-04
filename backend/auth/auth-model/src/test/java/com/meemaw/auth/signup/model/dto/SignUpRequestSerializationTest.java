package com.meemaw.auth.signup.model.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class SignUpRequestSerializationTest {

  @Test
  public void jacksonSerializationTest() throws JsonProcessingException {
    String payload =
        "{\"email\":\"test@gmail.com\",\"password\":\"password123\",\"fullName\":\"Marko Novak\",\"company\":\"Insight\"}";

    SignUpRequestDTO signUpRequestDTO =
        JacksonMapper.get().readValue(payload, SignUpRequestDTO.class);

    String deserialized = JacksonMapper.get().writeValueAsString(signUpRequestDTO);
    assertThat(
        "{\"email\":\"test@gmail.com\",\"password\":\"password123\",\"fullName\":\"Marko Novak\",\"company\":\"Insight\"}",
        sameJson(deserialized));
  }
}
