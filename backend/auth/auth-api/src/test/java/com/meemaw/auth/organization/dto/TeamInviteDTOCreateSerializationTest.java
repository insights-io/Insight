package com.meemaw.auth.organization.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class TeamInviteDTOCreateSerializationTest {

  @Test
  public void jackson___should_correctly_serialize_team_invite_create_dto()
      throws JsonProcessingException {
    TeamInviteCreateDTO teamInviteCreate =
        new TeamInviteCreateDTO("test@gmail.com", UserRole.MEMBER);
    String payload = JacksonMapper.get().writeValueAsString(teamInviteCreate);

    assertThat(payload, sameJson("{\"email\":\"test@gmail.com\",\"role\":\"member\"}"));

    TeamInviteCreateDTO deserialized =
        JacksonMapper.get().readValue(payload, TeamInviteCreateDTO.class);
    assertEquals(teamInviteCreate, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
