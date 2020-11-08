package com.rebrowse.model.organization;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.user.UserRole;

@Value
@Builder
public class TeamInviteCreateParams implements ApiRequestParams {

  String email;
  UserRole role;
}
