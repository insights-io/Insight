package com.rebrowse.model.user;

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

  public static CompletionStage<User> retrieve(RequestOptions requestOptions) {
    return ApiResource.get("/v1/user", User.class, requestOptions);
  }

  public static CompletionStage<User> retrieve(UUID id) {
    return retrieve(id, null);
  }

  public static CompletionStage<User> retrieve(UUID id, RequestOptions requestOptions) {
    return ApiResource.get(String.format("/v1/user/%s", id), User.class, requestOptions);
  }
}
