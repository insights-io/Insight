package com.rebrowse.model.organization;

import com.rebrowse.model.user.UserRole;
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
public class TeamInvite {

  UUID token;
  String email;
  String organizationId;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;

  public static CompletionStage<TeamInvite> create(TeamInviteCreateParams params) {
    return create(params, null);
  }

  public static CompletionStage<TeamInvite> create(
      TeamInviteCreateParams params, RequestOptions options) {
    return ApiResource.post("/v1/organization/invites", params, TeamInvite.class, options);
  }

  public CompletionStage<Void> accept(TeamInviteAcceptParams params) {
    return accept(token, params);
  }

  public CompletionStage<Void> accept(TeamInviteAcceptParams params, RequestOptions options) {
    return accept(token, params, options);
  }

  public static CompletionStage<Void> accept(UUID token, TeamInviteAcceptParams params) {
    return accept(token, params);
  }

  public static CompletionStage<Void> accept(
      UUID token, TeamInviteAcceptParams params, RequestOptions options) {
    String url = String.format("/v1/organization/invites/%s/accept", token);
    return ApiResource.post(url, params, Void.class, options);
  }
}
