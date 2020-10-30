package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.organization.service.OrganizationTeamInviteService;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrganizationTeamInviteResourceImpl implements OrganizationTeamInviteResource {

  @Inject InsightPrincipal principal;
  @Inject OrganizationTeamInviteService inviteService;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  private String getAcceptInviteURL() {
    URL clientBaseUrl =
        RequestUtils.parseRefererBaseURL(request)
            .orElseGet(() -> RequestUtils.getServerBaseURL(info, request));

    return UriBuilder.fromUri(clientBaseUrl.toString()).path("accept-invite").build().toString();
  }

  @Override
  public CompletionStage<Response> create(TeamInviteCreateDTO body) {
    AuthUser user = principal.user();
    return inviteService
        .createTeamInvite(body, user, getAcceptInviteURL())
        .thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> delete(UUID token) {
    return inviteService
        .deleteTeamInvite(token, principal)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> listAssociated() {
    return inviteService.listTeamInvites(principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> accept(UUID token, TeamInviteAcceptDTO body) {
    return inviteService
        .acceptTeamInvite(token, body)
        .thenApply((ignored) -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> send(UUID token) {
    return inviteService
        .sendTeamInvite(token, principal, getAcceptInviteURL())
        .thenApply(DataResponse::ok);
  }
}
