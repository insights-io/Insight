package com.rebrowse.model.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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
    return ApiResource.get("/v1/user", User.class, options);
  }

  public static CompletionStage<User> update(UserUpdateParams params, RequestOptions options) {
    return ApiResource.patch("/v1/user", params, User.class, options);
  }

  public static CompletionStage<User> retrieve(UUID id) {
    return retrieve(id, null);
  }

  public static CompletionStage<User> retrieve(UUID id, RequestOptions options) {
    return ApiResource.get(String.format("/v1/user/%s", id), User.class, options);
  }
}
