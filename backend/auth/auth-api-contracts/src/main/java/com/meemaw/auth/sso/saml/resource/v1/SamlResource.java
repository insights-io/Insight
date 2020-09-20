package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

  @GET
  @Path(OAuth2Resource.SIGNIN_PATH)
  CompletionStage<Response> signIn(
      @NotBlank(message = "Required") @Email @QueryParam("email") String email,
      @NotNull(message = "Required") @QueryParam("redirect") URL redirect);

  @POST
  @Path(OAuth2Resource.CALLBACK_PATH)
  CompletionStage<Response> callback(
      @NotBlank(message = "Required") @FormParam("SAMLResponse") String SAMLResponse,
      @NotBlank(message = "Required") @FormParam("RelayState") String RelayState,
      @CookieParam("state") String sessionState);
}
