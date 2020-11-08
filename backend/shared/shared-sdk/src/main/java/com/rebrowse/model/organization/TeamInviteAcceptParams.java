package com.rebrowse.model.organization;

import com.rebrowse.model.ApiRequestParams;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamInviteAcceptParams implements ApiRequestParams {

  String fullName;
  String password;
}
