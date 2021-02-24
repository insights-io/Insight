package com.rebrowse.auth.organization.model;

import com.rebrowse.auth.user.model.UserRole;
import lombok.Value;

@Value
public class TeamInviteTemplateData {

  String recipientEmail;
  UserRole recipientRole;
  String creatorFullName;
  String organizationName;
}
