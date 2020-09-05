package com.meemaw.auth.sso.openid.microsoft;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdClient;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class OpenIdMicrosoftClient
    extends AbstractOpenIdClient<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse> {

  private static final String TOKEN_SERVER_URL =
      "https://login.microsoftonline.com/common/oauth2/v2.0/token";
  private static final String USER_INFO_SERVER_URL = "https://graph.microsoft.com/oidc/userinfo";

  @Inject AppConfig appConfig;

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
        .putHeader("Authorization", "Bearer " + token.getAccessToken())
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String authorizationCode, String redirectUri) {
    MultiMap map = MultiMap.caseInsensitiveMultiMap();
    map.add("code", authorizationCode);
    map.add("redirect_uri", redirectUri);
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
