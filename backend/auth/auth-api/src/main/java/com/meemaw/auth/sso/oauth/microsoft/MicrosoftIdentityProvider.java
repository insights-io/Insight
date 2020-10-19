package com.meemaw.auth.sso.oauth.microsoft;

import com.meemaw.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class MicrosoftIdentityProvider
    extends AbstractOAuthIdentityProvider<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse> {

  private static final Collection<String> SCOPE_LIST = List.of("openid", "email", "profile");
  private static final String SCOPES = String.join(" ", SCOPE_LIST);
  private static final String AUTHORIZATION_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";

  @Inject MicrosoftOAuthClient client;

  @Override
  public LoginMethod getLoginMethod() {
    return LoginMethod.MICROSOFT;
  }

  /**
   * https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-implicit-grant-flow#send-the-sign-in-request
   *
   * @param state A value included in the request that will also be returned in the token response.
   *     It can be a string of any content that you wish. A randomly generated unique value is
   *     typically used for preventing cross-site request forgery attacks. The state is also used to
   *     encode information about the user's state in the app before the authentication request
   *     occurred, such as the page or view they were on.
   * @param serverRedirect The redirect_uri of your app, where authentication responses can be sent
   *     and received by your app. It must exactly match one of the redirect_uris you registered in
   *     the portal, except it must be url encoded.
   * @param loginHint Can be used to pre-fill the username/email address field of the sign in page
   *     for the user, if you know their username ahead of time. Often apps will use this parameter
   *     during re-authentication, having already extracted the username from a previous sign-in
   *     using the preferred_username claim.
   * @return constructed URI
   */
  @Override
  public URI buildAuthorizationUri(String state, URI serverRedirect, @Nullable String loginHint) {
    UriBuilder builder =
        UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
            .queryParam("client_id", appConfig.getMicrosoftOpenIdClientId())
            .queryParam("redirect_uri", serverRedirect)
            .queryParam("response_type", "code")
            .queryParam("scope", SCOPES)
            .queryParam("response_mode", "query");

    if (loginHint != null) {
      builder = builder.queryParam("login_hint", loginHint);
    }

    return builder.queryParam("state", state).build();
  }

  @Override
  @Traced
  @Timed(
      name = "oauth2callback",
      description = "A measure of how long it takes to do execute Microsoft oauth2callback")
  public CompletionStage<SsoLoginResult<?>> oauthCallback(
      String state, String sessionState, String code, URI serverBaseURI) {
    log.info("[AUTH]: OAuth2 callback request code={} serverBaseURI={}", code, serverBaseURI);
    return oauthCallback(client, state, sessionState, code, serverBaseURI);
  }
}
