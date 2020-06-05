package com.meemaw.auth.organization.invite.resource.v1;

import com.meemaw.auth.organization.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.organization.invite.service.InviteService;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeamInviteResourceImpl implements TeamInviteResource {

  @Inject InsightPrincipal principal;
  @Inject InviteService inviteService;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  private String getAcceptInviteURL() {
    String clientBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseGet(() -> RequestUtils.getServerBaseURL(info, request));

    return String.join("/", clientBaseURL, "accept-invite");
  }

  @Override
  public CompletionStage<Response> createTeamInvite(InviteCreateDTO body) {
    return inviteService
        .createTeamInvite(body, principal, getAcceptInviteURL())
        .thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> deleteTeamInvite(UUID token) {
    return inviteService.deleteTeamInvite(token, principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> listTeamInvites() {
    return inviteService.listTeamInvites(principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> acceptTeamInvite(UUID token, InviteAcceptDTO body) {
    return inviteService.acceptTeamInvite(token, body).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> sendTeamInvite(UUID token) {
    return inviteService
        .sendTeamInvite(token, principal, getAcceptInviteURL())
        .thenApply(x -> DataResponse.ok(true));
  }
}
