package com.meemaw.auth.org.invite.model;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.org.invite.model.dto.InviteAcceptDTO;
import com.meemaw.test.rest.mappers.JacksonMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class InviteAcceptTest {

  @Test
  public void dtoJacksonSerialization() throws JsonProcessingException {
    UUID token = UUID.fromString("bc2a1cc5-62ed-45a2-b7a6-70520dadc33b");
    InviteAcceptDTO teamInviteAccept =
        new InviteAcceptDTO("test@gmail.com", "ORG", token, "superPassword");

    String payload = JacksonMapper.get().writeValueAsString(teamInviteAccept);
    assertThat(
        payload,
        sameJson(
            "{\"token\":\"bc2a1cc5-62ed-45a2-b7a6-70520dadc33b\",\"email\":\"test@gmail.com\",\"password\":\"superPassword\",\"org\":\"ORG\"}"));

    InviteAcceptDTO deserialized = JacksonMapper.get().readValue(payload, InviteAcceptDTO.class);
    assertEquals(teamInviteAccept, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
