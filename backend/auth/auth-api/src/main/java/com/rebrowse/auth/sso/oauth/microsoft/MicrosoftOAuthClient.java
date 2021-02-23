package com.rebrowse.auth.sso.oauth.microsoft;

import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.rebrowse.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class MicrosoftOAuthClient
    extends AbstractOAuthClient<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse> {

  private static final String TOKEN_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/token";
  private static final String USER_INFO_SERVER_URL = "https://graph.microsoft.com/oidc/userinfo";

  @Inject
  AppConfig appConfig;

  @Override
  public Class<MicrosoftTokenResponse> getTokenClazz() {
    return MicrosoftTokenResponse.class;
  }

  @Override
  public Class<MicrosoftUserInfoResponse> getUserInfoClazz() {
    return MicrosoftUserInfoResponse.class;
  }

  @Override
  public Class<MicrosoftErrorResponse> getErrorClazz() {
    return MicrosoftErrorResponse.class;
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestUserInfo(MicrosoftTokenResponse token) {
    return webClient
        .getAbs(USER_INFO_SERVER_URL)
        .putHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String authorizationCode, URI redirect) {
    MultiMap map = MultiMap.caseInsensitiveMultiMap();
    map.add("code", authorizationCode);
    map.add("redirect_uri", redirect.toString());
    map.add("grant_type", "authorization_code");
    map.add("client_id", appConfig.getMicrosoftOpenIdClientId());
    map.add("client_secret", appConfig.getMicrosoftOpenIdClientSecret());

    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .putHeader("Content-Length", "0")
        .sendForm(map)
        .subscribeAsCompletionStage();
  }
}
