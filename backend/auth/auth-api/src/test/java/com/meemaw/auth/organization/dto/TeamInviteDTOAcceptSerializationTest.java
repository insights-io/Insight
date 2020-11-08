package com.meemaw.auth.organization.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.test.rest.mappers.JacksonMapper;

public class TeamInviteDTOAcceptSerializationTest {

  @Test
  public void jackson__should_correctly_serialize_team_invite_accept_dto()
      throws JsonProcessingException {
    TeamInviteAcceptDTO teamInviteAccept = new TeamInviteAcceptDTO("Marko Novak", "superPassword");
    String payload = JacksonMapper.get().writeValueAsString(teamInviteAccept);
    assertThat(payload, sameJson("{\"fullName\":\"Marko Novak\",\"password\":\"superPassword\"}"));
    TeamInviteAcceptDTO deserialized =
        JacksonMapper.get().readValue(payload, TeamInviteAcceptDTO.class);
    assertEquals(teamInviteAccept, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
