package com.rebrowse.model.organization;

import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PasswordPolicy {

  String organizationId;
  short minCharacters;
  boolean preventPasswordReuse;
  boolean requireUppercaseCharacter;
  boolean requireLowercaseCharacter;
  boolean requireNumber;
  boolean requireNonAlphanumericCharacter;
  OffsetDateTime updatedAt;
  OffsetDateTime createdAt;

  public static CompletionStage<PasswordPolicy> retrieve() {
    return retrieve(null);
  }

  public static CompletionStage<PasswordPolicy> retrieve(RequestOptions options) {
    return ApiResource.get("/v1/organization/password/policy", PasswordPolicy.class, options);
  }

  public static CompletionStage<PasswordPolicy> create(PasswordPolicyCreateParams params) {
    return create(params, null);
  }

  public static CompletionStage<PasswordPolicy> create(
      PasswordPolicyCreateParams params, RequestOptions options) {
    return ApiResource.post(
        "/v1/organization/password/policy", params, PasswordPolicy.class, options);
  }

  public static CompletionStage<PasswordPolicy> update(PasswordPolicyCreateParams params) {
    return update(params, null);
  }

  public static CompletionStage<PasswordPolicy> update(
      PasswordPolicyCreateParams params, RequestOptions options) {
    return ApiResource.patch(
        "/v1/organization/password/policy", params, PasswordPolicy.class, options);
  }
}
