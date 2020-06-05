package com.meemaw.auth.sso.resource.v1.google;

import com.meemaw.auth.sso.resource.v1.SsoResource;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(SsoGoogleResource.PATH)
public interface SsoGoogleResource {

  String PATH = SsoResource.PATH + "/google";
  String OAUTH2_CALLBACK_PATH = "oauth2callback";

  @GET
  @Path("signin")
  Response signIn(@NotBlank(message = "Required") @QueryParam("dest") String destination);

  @GET
  @Path(OAUTH2_CALLBACK_PATH)
  CompletionStage<Response> oauth2callback(
      @NotBlank(message = "Required") @QueryParam("state") String state,
      @NotBlank(message = "Required") @QueryParam("code") String code,
      @CookieParam("state") String sessionState);
}
