package com.meemaw.auth.sso.resource.v1;

import com.meemaw.shared.context.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface SsoOAuthResource {

  String CALLBACK_PATH = "oauth2callback";

  @GET
  @Path("signin")
  Response signIn(@NotBlank(message = "Required") @QueryParam("dest") String destination);

  @GET
  @Path(CALLBACK_PATH)
  CompletionStage<Response> oauth2callback(
      @NotBlank(message = "Required") @QueryParam("state") String state,
      @NotBlank(message = "Required") @QueryParam("code") String code,
      @CookieParam("state") String sessionState);

  String getBasePath();

  default String getRedirectUri(UriInfo info, HttpServerRequest request) {
    return RequestUtils.getServerBaseURL(info, request) + getBasePath() + "/" + CALLBACK_PATH;
  }
}
