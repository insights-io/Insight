package com.meemaw.auth.organization.model;

import lombok.Value;

import com.meemaw.auth.user.model.UserRole;

@Value
public class TeamInviteTemplateData {

  String recipientEmail;
  UserRole recipientRole;
  String creatorFullName;
  String organizationName;
}
