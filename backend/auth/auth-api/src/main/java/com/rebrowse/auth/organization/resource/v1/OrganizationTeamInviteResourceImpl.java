package com.rebrowse.auth.organization.resource.v1;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.organization.datasource.OrganizationTeamInviteTable;
import com.rebrowse.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.rebrowse.auth.organization.model.dto.TeamInviteCreateDTO;
import com.rebrowse.auth.organization.service.OrganizationTeamInviteService;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
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
    URI clientBaseUrl =
        RequestUtils.parseReferrerOrigin(request)
            .orElseGet(() -> RequestUtils.getServerBaseUri(uriInfo, request));
    return UriBuilder.fromUri(clientBaseUrl).path("accept-invite").build().toString();
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
    URI serverBaseUri = RequestUtils.getServerBaseUri(uriInfo, request);
    URI redirect = RequestUtils.sneakyUri(body.getRedirect());

    return inviteService
        .acceptTeamInvite(token, body)
        .thenCompose(
            (user) -> {
              AuthorizationRequest request =
                  new AuthorizationRequest(user.getEmail(), redirect, serverBaseUri);

              return ssoService
                  .tryAuthorizeNewUserDataResponse(user, request)
                  .thenApply(AuthorizationResponse::response);
            });
  }

  @Override
  public CompletionStage<Response> send(UUID token) {
    return inviteService
        .sendTeamInvite(token, principal, getAcceptInviteURL())
        .thenApply(DataResponse::ok);
  }
}
