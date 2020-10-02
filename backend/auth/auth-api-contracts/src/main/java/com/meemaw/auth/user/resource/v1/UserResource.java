package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(UserResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface UserResource {

  String PATH = "/v1/user";

  @GET
  @CookieAuth
  CompletionStage<Response> me(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @PATCH
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> update(@NotNull(message = "Required") Map<String, Object> body);

  @Path("phone_number/verify")
  @PATCH
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> verifyPhoneNumber(
      @NotNull(message = "Required") @Valid TfaChallengeCompleteDTO body);

  @POST
  @Path("phone_number/verify/send_code")
  @CookieAuth
  CompletionStage<Response> phoneNumberVerifySendCode();
}
