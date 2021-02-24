package com.rebrowse.auth.sso.oauth.microsoft;

import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.sso.oauth.AbstractOAuthIdentityProvider;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftScope;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import java.net.URI;
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

  private static final String AUTHORIZATION_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";

  private static final String SCOPES =
      String.join(
          " ",
          List.of(
              MicrosoftScope.OPENID.getValue(),
              MicrosoftScope.EMAIL.getValue(),
              MicrosoftScope.PROFILE.getValue()));

  @Inject MicrosoftOAuthClient client;

  @Override
  public SsoMethod getMethod() {
    return SsoMethod.MICROSOFT;
  }

  /**
   * https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-implicit-grant-flow#send-the-sign-in-request
   *
   * @param state A value included in the request that will also be returned in the token response.
   *     It can be a string of any content that you wish. A randomly generated unique value is
   *     typically used for preventing cross-site request forgery attacks. The state is also used to
   *     encode information about the user's state in the app before the authentication request
   *     occurred, such as the page or view they were on.
   * @param serverCallback The redirect_uri of your app, where authentication responses can be sent
   *     and received by your app. It must exactly match one of the redirect_uris you registered in
   *     the portal, except it must be url encoded.
   * @param loginHint Can be used to pre-fill the username/email address field of the sign in page
   *     for the user, if you know their username ahead of time. Often apps will use this parameter
   *     during re-authentication, having already extracted the username from a previous sign-in
   *     using the preferred_username claim.
   * @return constructed URI
   */
  @Override
  public URI buildAuthorizationUri(String state, URI serverCallback, @Nullable String loginHint) {
    UriBuilder builder =
        UriBuilder.fromUri(AUTHORIZATION_SERVER_URL)
            .queryParam("client_id", appConfig.getMicrosoftOpenIdClientId())
            .queryParam("redirect_uri", serverCallback)
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
  public CompletionStage<AuthorizationResponse> oauthCallback(
      String state, String sessionState, String code, URI serverBaseUri) {
    return oauthCallback(client, state, sessionState, code, serverBaseUri);
  }
}
