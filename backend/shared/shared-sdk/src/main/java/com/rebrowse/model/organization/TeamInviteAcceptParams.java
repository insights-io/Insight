package com.rebrowse.model.organization;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;

@Value
@Builder
public class TeamInviteAcceptParams implements ApiRequestParams {

  String fullName;
  String password;
}
