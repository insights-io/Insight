package com.meemaw.auth.org.invite.model.dto;

import com.meemaw.auth.org.invite.model.CanInviteSend;
import com.meemaw.auth.shared.CanExpire;
import com.meemaw.auth.user.model.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class InviteDTO implements CanInviteSend, CanExpire {

  UUID token;
  String email;
  String org;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;
}
