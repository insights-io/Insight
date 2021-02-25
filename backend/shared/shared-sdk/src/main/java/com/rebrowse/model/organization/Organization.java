package com.rebrowse.model.organization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rebrowse.model.user.User;
import com.rebrowse.model.user.UserRole;
import com.rebrowse.model.user.UserSearchParams;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Organization implements IOrganization {

  String id;
  String name;
  boolean openMembership;
  boolean enforceMultiFactorAuthentication;
  UserRole defaultRole;
  AvatarType avatar;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;

  public static CompletionStage<Organization> retrieve(RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET, "/v1/organization", Organization.class, requestOptions);
  }

  public static CompletionStage<Organization> retrieve(String id, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.GET,
        String.format("/v1/organization/%s", id),
        Organization.class,
        requestOptions);
  }

  public static CompletionStage<Organization> update(
      OrganizationUpdateParams params, RequestOptions requestOptions) {
    return ApiResource.request(
        RequestMethod.PATCH, "/v1/organization", params, Organization.class, requestOptions);
  }

  public static CompletionStage<AvatarSetup> updateAvatar(AvatarSetupUpdateParams params) {
    return updateAvatar(params, null);
  }

  public static CompletionStage<AvatarSetup> updateAvatar(
      AvatarSetupUpdateParams params, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.PATCH, "/v1/organization/avatar", params, AvatarSetup.class, options);
  }

  public static CompletionStage<List<User>> members(
      UserSearchParams params, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.GET, "/v1/organization/members", params, new TypeReference<>() {}, options);
  }
}
