package com.rebrowse.auth.sso.oauth.github;

import com.rebrowse.auth.core.config.model.AppConfig;
import com.rebrowse.auth.sso.oauth.AbstractOAuthClient;
import com.rebrowse.auth.sso.oauth.github.model.GithubErrorResponse;
import com.rebrowse.auth.sso.oauth.github.model.GithubTokenResponse;
import com.rebrowse.auth.sso.oauth.github.model.GithubUserInfoResponse;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class GithubOAuthClient
    extends AbstractOAuthClient<GithubTokenResponse, GithubUserInfoResponse, GithubErrorResponse> {

  private static final String TOKEN_SERVER_URL = "https://github.com/login/oauth/access_token";
  private static final String TOKEN_INFO_SERVER_URL = "https://api.github.com/user";

  @Inject AppConfig appConfig;

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestCodeExchange(
      String code, URI serverRedirectURI) {
    return webClient
        .postAbs(TOKEN_SERVER_URL)
        .addQueryParam("code", code)
        .addQueryParam("client_id", appConfig.getGithubOpenIdClientId())
        .addQueryParam("client_secret", appConfig.getGithubOpenIdClientSecret())
        .addQueryParam("redirect_uri", serverRedirectURI.toString())
        .putHeader("Content-Length", "0")
        .putHeader("Accept", MediaType.APPLICATION_JSON)
        .send()
        .subscribeAsCompletionStage();
  }

  @Override
  protected CompletionStage<HttpResponse<Buffer>> requestUserInfo(GithubTokenResponse token) {
    return webClient
        .getAbs(TOKEN_INFO_SERVER_URL)
        .putHeader(HttpHeaders.AUTHORIZATION, "token " + token.getAccessToken())
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
