package com.rebrowse.model.auth;

import com.rebrowse.Rebrowse;
import com.rebrowse.model.user.User;
import com.rebrowse.net.ApiResource;
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
  String value;
  OffsetDateTime createdAt;

  public static CompletionStage<User> retrieveUser() {
    return retrieveUser(null);
  }

  public static CompletionStage<User> retrieveUser(RequestOptions requestOptions) {
    String url = String.format("%s%s", Rebrowse.apiBase(), "/v1/sso/auth/token/user");
    return ApiResource.get(url, User.class, requestOptions);
  }

  public static CompletionStage<ApiKey> create(RequestOptions requestOptions) {
    String url = String.format("%s%s", Rebrowse.apiBase(), "/v1/sso/auth/token");
    return ApiResource.post(url, ApiKey.class, requestOptions);
  }
}
