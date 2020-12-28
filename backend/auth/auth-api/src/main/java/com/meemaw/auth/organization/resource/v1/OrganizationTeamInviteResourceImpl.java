package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.datasource.OrganizationTeamInviteTable;
import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.organization.service.OrganizationTeamInviteService;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.response.Boom;
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

  @Inject AuthPrincipal principal;
  @Inject OrganizationTeamInviteService inviteService;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;
  @Context UriInfo uriInfo;

  private String getAcceptInviteURL() {
    URL clientBaseUrl =
        RequestUtils.parseReferrerOrigin(request)
            .orElseGet(() -> RequestUtils.getServerBaseUrl(uriInfo, request));

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
  public CompletionStage<Response> retrieve(UUID token) {
    return inviteService
        .retrieve(token)
        .thenApply(
            maybeTeamInvite ->
                maybeTeamInvite
                    .map(DataResponse::ok)
                    .orElseThrow(() -> Boom.notFound().exception()));
  }

  @Override
  public CompletionStage<Response> delete(UUID token) {
    return inviteService
        .deleteTeamInvite(token, principal)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> listAssociated() {
    SearchDTO search =
        SearchDTO.withAllowedFields(OrganizationTeamInviteTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return inviteService.listTeamInvites(principal, search).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> count() {
    SearchDTO search =
        SearchDTO.withAllowedFields(OrganizationTeamInviteTable.QUERYABLE_FIELDS)
            .rhsColon(RequestUtils.map(uriInfo.getQueryParameters()));

    return inviteService.count(principal, search).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> accept(UUID token, TeamInviteAcceptDTO body) {
    URL serverBaseUrl = RequestUtils.getServerBaseUrl(uriInfo, request);
    String cookieDomain = RequestUtils.parseCookieDomain(serverBaseUrl);
    return inviteService
        .acceptTeamInvite(token, body)
        .thenCompose(
            (user) ->
                ssoService
                    .authenticate(user)
                    .thenApply(loginResult -> loginResult.loginResponse(cookieDomain)));
  }

  @Override
  public CompletionStage<Response> send(UUID token) {
    return inviteService
        .sendTeamInvite(token, principal, getAcceptInviteURL())
        .thenApply(DataResponse::ok);
  }
}
