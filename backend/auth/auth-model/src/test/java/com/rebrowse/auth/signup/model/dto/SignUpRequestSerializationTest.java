package com.rebrowse.auth.signup.model.dto;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class SignUpRequestSerializationTest {

  @Test
  public void jacksonSerializationTest() throws JsonProcessingException {
    String payload =
        "{\"email\":\"test@gmail.com\",\"password\":\"password123\",\"fullName\":\"Marko Novak\",\"company\":\"Google\", \"phoneNumber\": {\"countryCode\": \"+386\", \"digits\": \"51111111\"}}";

    SignUpRequestDTO signUpRequestDTO =
        JacksonMapper.get().readValue(payload, SignUpRequestDTO.class);

    String deserialized = JacksonMapper.get().writeValueAsString(signUpRequestDTO);
    assertThat(
        "{\"email\":\"test@gmail.com\",\"password\":\"password123\",\"fullName\":\"Marko Novak\",\"company\":\"Google\", \"phoneNumber\": {\"countryCode\": \"+386\", \"digits\": \"51111111\"}}",
        sameJson(deserialized));
  }
}
