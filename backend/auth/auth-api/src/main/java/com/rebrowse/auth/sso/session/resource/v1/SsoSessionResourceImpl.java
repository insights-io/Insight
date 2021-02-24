package com.rebrowse.auth.sso.session.resource.v1;

import com.rebrowse.auth.organization.service.OrganizationService;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.dto.UserDataDTO;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoSessionResourceImpl implements SsoSessionResource {

  @Inject OrganizationService organizationService;
  @Inject SsoService ssoService;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  @Override
  public CompletionStage<Response> logout(String sessionId) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    String domain = URIUtils.parseCookieDomain(serverBaseUri);

    return ssoService
        .logout(sessionId)
        .thenApply(
            maybeUser -> {
              ResponseBuilder builder =
                  maybeUser.isPresent()
                      ? Response.noContent()
                      : DataResponse.error(Boom.notFound()).builder();

              return builder.cookie(SsoSession.clearCookie(domain)).build();
            });
  }

  @Override
  public CompletionStage<Response> logoutFromAllDevices(String sessionId) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    String cookieDomain = URIUtils.parseCookieDomain(serverBaseUri);

    return ssoService
        .logoutFromAllDevices(sessionId)
        .thenApply(
            sessions -> Response.noContent().cookie(SsoSession.clearCookie(cookieDomain)).build());
  }

  @Override
  public CompletionStage<Response> retrieveUserData(String sessionId) {
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    String cookieDomain = URIUtils.parseCookieDomain(serverBaseUri);

    return ssoService
        .retrieveSession(sessionId)
        .thenCompose(
            maybeUser -> {
              if (maybeUser.isEmpty()) {
                return CompletableFuture.completedStage(
                    Boom.notFound()
                        .responseBuilder()
                        .cookie(SsoSession.clearCookie(cookieDomain))
                        .build());
              }

              AuthUser user = maybeUser.get();
              String organizationId = user.getOrganizationId();
              return organizationService
                  .getOrganization(organizationId)
                  .thenApply(
                      maybeOrganization -> {
                        if (maybeOrganization.isEmpty()) {
                          return Boom.notFound()
                              .responseBuilder()
                              .cookie(SsoSession.clearCookie(cookieDomain))
                              .build();
                        }

                        return DataResponse.ok(UserDataDTO.from(user, maybeOrganization.get()));
                      });
            });
  }

  @Override
  public CompletionStage<Response> retrieveUserDataByCookieParam(String sessionId) {
    return retrieveUserData(sessionId);
  }

  @Override
  public CompletionStage<Response> listAssociatedSessions(String sessionId) {
    return ssoService.listSessions(sessionId).thenApply(DataResponse::ok);
  }
}
