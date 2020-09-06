package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.sso.resource.v1.SsoResource;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(SamlResource.PATH)
public interface SamlResource {

  String PATH = SsoResource.PATH + "/saml";
  String CALLBACK_PATH = "callback";
  String SIGNIN_PATH = "signin";

  @GET
  @Path(SIGNIN_PATH)
  Response signIn(@NotBlank(message = "Required") @QueryParam("dest") String destination);

  @POST
  @Path(CALLBACK_PATH)
  CompletionStage<Response> callback(
      @NotBlank(message = "Required") @FormParam("SAMLResponse") String SAMLResponse,
      @NotBlank(message = "Required") @FormParam("RelayState") String RelayState,
      @CookieParam("state") String sessionState);
}
