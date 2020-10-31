package com.rebrowse.model.auth;

import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SessionInfo {

  User user;
  Organization organization;

  public static CompletionStage<SessionInfo> retrieve() {
    return retrieve(null);
  }

  public static CompletionStage<SessionInfo> retrieve(RequestOptions requestOptions) {
    return ApiResource.get("/v1/sso/session", SessionInfo.class, requestOptions);
  }
}
