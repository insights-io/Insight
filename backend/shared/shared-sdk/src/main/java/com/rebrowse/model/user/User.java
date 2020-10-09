package com.rebrowse.model.user;

import com.rebrowse.Rebrowse;
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
    String url = String.format("%s%s", Rebrowse.apiBase(), "/v1/user");
    return ApiResource.get(url, User.class, requestOptions);
  }

  public static CompletionStage<User> retrieve(UUID id) {
    return retrieve(id, null);
  }

  public static CompletionStage<User> retrieve(UUID id, RequestOptions requestOptions) {
    String url = String.format("%s%s%s", Rebrowse.apiBase(), "/v1/user/", id);
    return ApiResource.get(url, User.class, requestOptions);
  }
}
