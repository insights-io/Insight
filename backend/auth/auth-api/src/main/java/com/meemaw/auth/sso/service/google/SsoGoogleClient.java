package com.meemaw.auth.sso.service.google;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.model.google.GoogleErrorResponse;
import com.meemaw.auth.sso.model.google.GoogleTokenResponse;
import com.meemaw.auth.sso.model.google.GoogleUserInfoResponse;
import com.meemaw.auth.sso.service.AbstractSsoOAuthClient;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SsoGoogleClient
    extends AbstractSsoOAuthClient<
        GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse> {

  private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
  private static final String TOKEN_INFO_SERVER_URL = "https://oauth2.googleapis.com/tokeninfo";

  @Inject AppConfig appConfig;

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestUserInfo(GoogleTokenResponse token) {
    return webClient
        .getAbs(TOKEN_INFO_SERVER_URL)
        .addQueryParam("id_token", token.getIdToken())
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String code, String redirectUri) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("grant_type", "authorization_code")
        .addQueryParam("code", code)
        .addQueryParam("client_id", appConfig.getGoogleOAuthClientId())
        .addQueryParam("client_secret", appConfig.getGoogleOAuthClientSecret())
        .addQueryParam("redirect_uri", redirectUri)
        .putHeader("Content-Length", "0")
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  public Class<GoogleTokenResponse> getTokenClazz() {
    return GoogleTokenResponse.class;
  }

  @Override
  public Class<GoogleUserInfoResponse> getUserInfoClazz() {
    return GoogleUserInfoResponse.class;
  }

  @Override
  public Class<GoogleErrorResponse> getErrorClazz() {
    return GoogleErrorResponse.class;
  }
}
