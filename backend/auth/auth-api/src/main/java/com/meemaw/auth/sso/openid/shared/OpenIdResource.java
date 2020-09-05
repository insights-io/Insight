package com.meemaw.auth.sso.openid.shared;

import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface OpenIdResource {

  String CALLBACK_PATH = "oauth2callback";
  String SIGNIN_PATH = "signin";

  @GET
  @Path(SIGNIN_PATH)
  Response signIn(@NotBlank(message = "Required") @QueryParam("dest") String destination);

  @GET
  @Path(CALLBACK_PATH)
  CompletionStage<Response> oauth2callback(
      @NotBlank(message = "Required") @QueryParam("state") String state,
      @NotBlank(message = "Required") @QueryParam("code") String code,
      @CookieParam("state") String sessionState);
}
