package com.meemaw.auth.org.invite.model.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class InviteSendIdentifiedDTO {

  String email;
  String org;
  UUID creator;
}
