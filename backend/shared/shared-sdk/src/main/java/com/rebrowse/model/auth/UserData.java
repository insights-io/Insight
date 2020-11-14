package com.rebrowse.model.auth;

import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserData {

  User user;
  Organization organization;

  public static CompletionStage<UserData> retrieve() {
    return retrieve((RequestOptions) null);
  }

  public static CompletionStage<UserData> retrieve(RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET, "/v1/sso/session/userdata", UserData.class, requestOptions);
  }

  public static CompletionStage<UserData> retrieve(String sessionId) {
    return retrieve(sessionId, null);
  }

  public static CompletionStage<UserData> retrieve(
      String sessionId, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET,
        String.format("/v1/sso/session/%s/userdata", sessionId),
        UserData.class,
        requestOptions);
  }
}
