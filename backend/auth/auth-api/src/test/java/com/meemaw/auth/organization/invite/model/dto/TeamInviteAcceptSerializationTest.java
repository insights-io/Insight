package com.meemaw.auth.organization.invite.model.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class TeamInviteAcceptSerializationTest {

  @Test
  public void jacksonSerialization() throws JsonProcessingException {
    InviteAcceptDTO teamInviteAccept = new InviteAcceptDTO("Marko Novak", "superPassword");
    String payload = JacksonMapper.get().writeValueAsString(teamInviteAccept);
    assertThat(payload, sameJson("{\"fullName\":\"Marko Novak\",\"password\":\"superPassword\"}"));
    InviteAcceptDTO deserialized = JacksonMapper.get().readValue(payload, InviteAcceptDTO.class);
    assertEquals(teamInviteAccept, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
