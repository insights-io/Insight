package com.rebrowse.auth.organization.dto;

import static com.rebrowse.test.matchers.SameJSON.sameJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.rebrowse.test.rest.mappers.JacksonMapper;
import java.net.MalformedURLException;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class TeamInviteDTOAcceptSerializationTest {

  @Test
  public void jackson__should_correctly_serialize_team_invite_accept_dto()
      throws JsonProcessingException, MalformedURLException {
    TeamInviteAcceptDTO teamInviteAccept =
        new TeamInviteAcceptDTO("Marko Novak", "superPassword", new URL("http://localhost:3000"));
    String payload = JacksonMapper.get().writeValueAsString(teamInviteAccept);
    MatcherAssert.assertThat(
        payload,
        sameJson(
            "{\"fullName\":\"Marko Novak\",\"password\":\"superPassword\", \"redirect\": \"http://localhost:3000\"}"));
    TeamInviteAcceptDTO deserialized =
        JacksonMapper.get().readValue(payload, TeamInviteAcceptDTO.class);
    assertEquals(teamInviteAccept, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
