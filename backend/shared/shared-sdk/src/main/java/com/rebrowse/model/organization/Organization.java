package com.rebrowse.model.organization;

import com.rebrowse.model.user.UserRole;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Organization {

  String id;
  String name;
  boolean openMembership;
  UserRole defaultRole;
  AvatarType avatar;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;

  public static CompletionStage<Organization> retrieve(RequestOptions requestOptions) {
    return ApiResource.get("/v1/organization", Organization.class, requestOptions);
  }

  public static CompletionStage<Organization> retrieve(String id, RequestOptions requestOptions) {
    return ApiResource.get(
        String.format("/v1/organization/%s", id), Organization.class, requestOptions);
  }

  public static CompletionStage<Organization> update(
      OrganizationUpdateParams params, RequestOptions requestOptions) {
    return ApiResource.patch("/v1/organization", params, Organization.class, requestOptions);
  }
}
