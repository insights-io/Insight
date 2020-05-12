package com.meemaw.auth.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserDtoSerializationTest {

  @Test
  public void userDtoSerializationTest() throws JsonProcessingException {
    String payload =
        "{\"id\":\"25320051-1c63-4d4e-a38c-c1f7f6da0896\",\"email\":\"test-user@gmail.com\",\"role\":\"STANDARD\",\"org\":\"org123\"}";
    UserDTO deserialized = JacksonMapper.get().readValue(payload, UserDTO.class);
    assertEquals(UserDTO.class, deserialized.getClass());

    assertEquals(UUID.fromString("25320051-1c63-4d4e-a38c-c1f7f6da0896"), deserialized.getId());
  }
}
