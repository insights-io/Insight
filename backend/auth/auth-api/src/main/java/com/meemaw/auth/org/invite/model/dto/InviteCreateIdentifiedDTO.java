package com.meemaw.auth.org.invite.model.dto;

import com.meemaw.auth.org.invite.model.CanInviteSend;
import com.meemaw.auth.user.model.UserRole;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class InviteCreateIdentifiedDTO implements CanInviteSend {

  String email;
  String org;
  UserRole role;
  UUID creator;
}
