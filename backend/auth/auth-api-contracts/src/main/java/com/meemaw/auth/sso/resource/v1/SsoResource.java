package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.shared.validation.Password;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SsoResource.PATH)
@RegisterRestClient(configKey = "sso-resource")
public interface SsoResource {

  String PATH = "/v1/sso";

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  CompletionStage<Response> login(
      @NotBlank(message = "Required") @Email @FormParam("email") String email,
      @Password @FormParam("password") String password);

  @POST
  @Path("logout")
  CompletionStage<Response> logout(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @GET
  @Path("session")
  @Produces(MediaType.APPLICATION_JSON)
  CompletionStage<Response> session(
      @NotNull(message = "Required") @QueryParam("id") String sessionId);

  @GET
  @Path("me")
  @Produces(MediaType.APPLICATION_JSON)
  default CompletionStage<Response> me(
      @NotNull(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId) {
    return session(sessionId);
  }
}
