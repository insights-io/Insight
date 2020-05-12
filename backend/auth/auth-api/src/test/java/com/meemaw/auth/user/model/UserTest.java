package com.meemaw.auth.user.model;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void dtoJacksonSerialization() throws JsonProcessingException {
    UUID userId = UUID.fromString("bc2a1cc5-62ed-45a2-b7a6-70520dadc33b");
    UserDTO userDTO = new UserDTO(userId, "test@gmail.com", UserRole.ADMIN, "ORG");
    String payload = JacksonMapper.get().writeValueAsString(userDTO);
    assertThat(
        payload,
        sameJson(
            "{\"id\":\"bc2a1cc5-62ed-45a2-b7a6-70520dadc33b\",\"email\":\"test@gmail.com\",\"role\":\"ADMIN\",\"org\":\"ORG\"}"));

    UserDTO deserialized = JacksonMapper.get().readValue(payload, UserDTO.class);
    assertEquals(userDTO, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
