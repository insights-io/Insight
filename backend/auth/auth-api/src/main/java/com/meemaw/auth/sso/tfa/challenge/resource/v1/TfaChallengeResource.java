package com.meemaw.auth.sso.tfa.challenge.resource.v1;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
import com.meemaw.auth.sso.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.sso.tfa.setup.resource.v1.TfaResource;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(TfaChallengeResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface TfaChallengeResource {

  String PATH = TfaResource.PATH + "/challenge";

  @POST
  @Path("{method}/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> complete(
      @PathParam("method") TfaMethod method,
      @NotBlank(message = "Required") @CookieParam(SsoChallenge.COOKIE_NAME) String challengeId,
      @NotNull(message = "Required") @Valid TfaChallengeCompleteDTO body);

  @GET
  CompletionStage<Response> get(
      @NotNull(message = "Required") @QueryParam("id") String challengeId);

  @POST
  @Path("sms/send_code")
  CompletionStage<Response> sendCode(
      @NotBlank(message = "Required") @CookieParam(SsoChallenge.COOKIE_NAME) String challengeId);
}
