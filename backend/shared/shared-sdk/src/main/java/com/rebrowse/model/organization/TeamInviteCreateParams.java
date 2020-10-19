package com.rebrowse.model.organization;

import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.user.UserRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamInviteCreateParams implements ApiRequestParams {

  String email;
  UserRole role;
}
