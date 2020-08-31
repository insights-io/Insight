package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(UserResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface UserResource {

  String PATH = "/v1/user";

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
