package com.meemaw.auth.sso.oauth.shared;

import com.meemaw.auth.sso.resource.v1.SsoResource;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface OAuth2Resource {

  String PATH = SsoResource.PATH + "/oauth2";
  String CALLBACK_PATH = "callback";
  String SIGNIN_PATH = "signin";

  @GET
  @Path(SIGNIN_PATH)
  Response signIn(@NotNull(message = "Required") @QueryParam("redirect") URL redirect);

  @GET
  @Path(CALLBACK_PATH)
  CompletionStage<Response> oauth2callback(
      @NotBlank(message = "Required") @QueryParam("code") String code,
      @NotBlank(message = "Required") @QueryParam("state") String state,
      @CookieParam("state") String sessionState);
}
