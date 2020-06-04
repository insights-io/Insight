package com.meemaw.auth.organization.invite.model;

import com.meemaw.auth.user.model.UserRole;
import lombok.Value;

@Value
public class TeamInviteTemplateData {

  String recipientEmail;
  UserRole recipientRole;
  String creatorFullName;
  String organizationName;
}
