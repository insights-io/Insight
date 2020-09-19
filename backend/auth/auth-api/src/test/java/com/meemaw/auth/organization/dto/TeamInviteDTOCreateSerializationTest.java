package com.meemaw.auth.organization.dto;

import static com.meemaw.test.matchers.SameJSON.sameJson;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.meemaw.auth.organization.model.dto.InviteCreateDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.test.rest.mappers.JacksonMapper;
import org.junit.jupiter.api.Test;

public class TeamInviteDTOCreateSerializationTest {

  @Test
  public void jackson___should_correctly_serialize_invite_create_dto()
      throws JsonProcessingException {
    InviteCreateDTO teamInviteCreate = new InviteCreateDTO("test@gmail.com", UserRole.STANDARD);
    String payload = JacksonMapper.get().writeValueAsString(teamInviteCreate);

    assertThat(payload, sameJson("{\"email\":\"test@gmail.com\",\"role\":\"standard\"}"));

    InviteCreateDTO deserialized = JacksonMapper.get().readValue(payload, InviteCreateDTO.class);
    assertEquals(teamInviteCreate, deserialized);
    assertEquals(payload, JacksonMapper.get().writeValueAsString(deserialized));
  }
}
