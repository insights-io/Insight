package com.rebrowse.model.auth;

import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SsoSetup {

  String organizationId;
  String domain;
  SsoMethod method;
  URL configurationEndpoint;
  OffsetDateTime createdAt;

  public static CompletionStage<SsoSetup> create(SsoSetupCreateParams params) {
    return create(params, null);
  }

  public static CompletionStage<SsoSetup> create(
      SsoSetupCreateParams params, RequestOptions options) {
    return ApiResource.post("/v1/sso/setup", params, SsoSetup.class, options);
  }
}
