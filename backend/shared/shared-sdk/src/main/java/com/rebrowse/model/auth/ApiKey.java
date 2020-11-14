package com.rebrowse.model.auth;

import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ApiKey {

  UUID userId;
  String token;
  OffsetDateTime createdAt;

  public static CompletionStage<User> retrieveUser() {
    return retrieveUser(null);
  }

  public static CompletionStage<User> retrieveUser(RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET, "/v1/sso/auth/token/user", User.class, requestOptions);
  }

  public static CompletionStage<ApiKey> create(RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.POST, "/v1/sso/auth/token", ApiKey.class, requestOptions);
  }
}
