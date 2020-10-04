package com.meemaw.auth.sso.session.resource.v1;

import com.meemaw.auth.sso.session.model.SsoSession;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SsoResource.PATH)
@RegisterRestClient(configKey = "auth-api")
public interface SsoResource {

  String PATH = "/v1/sso";
  String TAG = "Session";

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Login")
  CompletionStage<Response> login(
      @NotBlank(message = "Required") @Email @FormParam("email") String email,
      @Password @FormParam("password") String password);

  @POST
  @Path("logout")
  @Tag(name = TAG)
  @Operation(summary = "Logout")
  CompletionStage<Response> logout(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @POST
  @Path("logout-from-all-devices")
  @Tag(name = TAG)
  @Operation(summary = "Logout from all devices")
  CompletionStage<Response> logoutFromAllDevices(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @GET
  @Path("sessions")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "List sessions associated with authenticated user")
  CompletionStage<Response> listAssociatedSessions(
      @NotNull(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @GET
  @Path("session/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Retrieve session")
  CompletionStage<Response> getSession(@PathParam("id") String sessionId);

  @GET
  @Path("session")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Retrieve session associated with authenticated user")
  default CompletionStage<Response> getAssociatedSession(
      @NotNull(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId) {
    return getSession(sessionId);
  }
}
