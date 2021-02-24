package com.rebrowse.auth.sso.oauth.google;

import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.oauth.google.model.GoogleErrorResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleTokenResponse;
import com.rebrowse.auth.sso.oauth.google.model.GoogleUserInfoResponse;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class GoogleOAuthClient
    extends AbstractOAuthClient<GoogleTokenResponse, GoogleUserInfoResponse, GoogleErrorResponse> {

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
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(String code, URI redirect) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("grant_type", "authorization_code")
        .addQueryParam("code", code)
        .addQueryParam("client_id", appConfig.getGoogleOpenIdClientId())
        .addQueryParam("client_secret", appConfig.getGoogleOpenIdClientSecret())
        .addQueryParam("redirect_uri", redirect.toString())
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
