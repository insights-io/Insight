package com.meemaw.auth.sso.openid.github;

import com.meemaw.auth.core.config.model.AppConfig;
import com.meemaw.auth.sso.openid.github.model.GithubErrorResponse;
import com.meemaw.auth.sso.openid.github.model.GithubTokenResponse;
import com.meemaw.auth.sso.openid.github.model.GithubUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdClient;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OpenIdGithubClient
    extends AbstractOpenIdClient<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse> {

  private static final String TOKEN_SERVER_URL = "https://github.com/login/oauth/access_token";
  private static final String TOKEN_INFO_SERVER_URL = "https://api.github.com/user";

  @Inject AppConfig appConfig;

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String code, String redirectUri) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("code", code)
        .addQueryParam("client_id", appConfig.getGithubOpenIdClientId())
        .addQueryParam("client_secret", appConfig.getGithubOpenIdClientSecret())
        .addQueryParam("redirect_uri", redirectUri)
        .putHeader("Content-Length", "0")
        .putHeader("Accept", MediaType.APPLICATION_JSON)
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestUserInfo(GithubTokenResponse token) {
    return webClient
        .getAbs(TOKEN_INFO_SERVER_URL)
        .putHeader("Authorization", "token " + token.getAccessToken())
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  public Class<GithubTokenResponse> getTokenClazz() {
    return GithubTokenResponse.class;
  }

  @Override
  public Class<GithubUserInfoResponse> getUserInfoClazz() {
    return GithubUserInfoResponse.class;
  }

  @Override
  public Class<GithubErrorResponse> getErrorClazz() {
    return GithubErrorResponse.class;
  }
}
