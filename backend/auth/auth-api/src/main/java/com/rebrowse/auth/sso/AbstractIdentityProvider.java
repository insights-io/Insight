package com.rebrowse.auth.sso;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.request.SsoAuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.sso.oauth.OAuthResource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.api.RebrowseApi;
import java.net.URI;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractIdentityProvider {

  public static final int SECURE_STATE_PREFIX_LENGTH = 26;
  private static final SecureRandom random = new SecureRandom();

  public static String secureState(String data) {
    return secureState() + data;
  }

  public static String secureState() {
    return RandomStringUtils.random(SECURE_STATE_PREFIX_LENGTH, 0, 0, true, true, null, random);
  }

  /**
   * Extract data encoded in a secure state by stripping the prefix of fixed length.
   *
   * @param secureState from authorization flow
   * @return data that was encoded in the state
   */
  public static String secureStateData(String secureState) {
    try {
      return URLDecoder.decode(
          secureState.substring(SECURE_STATE_PREFIX_LENGTH), RebrowseApi.CHARSET);
    } catch (StringIndexOutOfBoundsException ex) {
      throw Boom.badRequest().message("Invalid state parameter").exception(ex);
    }
  }

  public abstract SsoMethod getMethod();

  public abstract String getResourcePath();

  public AuthorizationResponse ssoErrorAuthorizationResponse(
      Throwable throwable, String domain, URI redirect) {
    String message = throwable.getCause().getMessage();
    URI location = UriBuilder.fromUri(redirect).queryParam("oauthError", message).build();
    return (cookies) ->
        Response.status(Status.FOUND)
            .cookie(SsoSession.clearCookie(domain))
            .location(location)
            .build();
  }

  public String getCallbackPath() {
    return String.join("/", getResourcePath(), OAuthResource.CALLBACK_PATH);
  }

  public URI getCallbackEndpoint(URI serverBaseUri) {
    return UriBuilder.fromUri(serverBaseUri).path(getCallbackPath()).build();
  }

  public URI getSignInEndpoint(URI serverBaseUri) {
    return UriBuilder.fromUri(serverBaseUri).path(getSignInPath()).build();
  }

  public String getSignInPath() {
    return String.join("/", getResourcePath(), OAuthResource.SIGNIN_PATH);
  }

  public abstract URI buildAuthorizationUri(
      String state, URI serverRedirectUri, @Nullable String loginHint);

  public CompletionStage<AuthorizationResponse> getSsoAuthorizationRedirectResponse(
          SsoSetupDTO ssoSetup, AuthorizationRequest request) {
    return getSsoAuthorizationRequest(ssoSetup, request).thenApply(v -> v);
  }

  public abstract CompletionStage<SsoAuthorizationRequest> getSsoAuthorizationRequest(
      SsoSetupDTO ssoSetup, AuthorizationRequest request);
}
