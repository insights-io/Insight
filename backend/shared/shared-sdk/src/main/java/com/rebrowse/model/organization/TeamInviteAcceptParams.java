package com.rebrowse.model.organization;

import com.rebrowse.model.ApiRequestParams;
import java.net.URL;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamInviteAcceptParams implements ApiRequestParams {

  String fullName;
  String password;
  URL redirect;
}
