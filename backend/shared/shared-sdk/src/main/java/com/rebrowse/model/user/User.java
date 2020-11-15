package com.rebrowse.model.user;

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
public class User {

  UUID id;
  String email;
  String fullName;
  UserRole role;
  String organizationId;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
  PhoneNumber phoneNumber;
  boolean phoneNumberVerified;

  public static CompletionStage<User> retrieve() {
    return retrieve((RequestOptions) null);
  }

  public static CompletionStage<User> retrieve(RequestOptions options) {
    return ApiResource.request(RequestMethod.GET, "/v1/user", User.class, options);
  }

  public static CompletionStage<User> retrieve(UUID id) {
    return retrieve(id, null);
  }

  public static CompletionStage<User> retrieve(UUID id, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.GET, String.format("/v1/user/%s", id), User.class, options);
  }

  public static CompletionStage<User> update(UserUpdateParams params, RequestOptions options) {
    return ApiResource.request(RequestMethod.PATCH, "/v1/user", params, User.class, options);
  }

  public static CompletionStage<User> updatePhoneNumber(
      PhoneNumberUpdateParams params, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.PATCH, "/v1/user/phone_number", params, User.class, options);
  }
}
