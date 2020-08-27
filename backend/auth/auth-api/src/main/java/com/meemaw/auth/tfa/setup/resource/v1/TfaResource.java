package com.meemaw.auth.tfa.setup.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(TfaResource.PATH)
public interface TfaResource {

  String PATH = "/v1/tfa";

  @GET
  @CookieAuth
  CompletionStage<Response> list();

  @GET
  @Path("{method}")
  @CookieAuth
  CompletionStage<Response> get(@PathParam("method") TfaMethod method);

  @DELETE
  @CookieAuth
  @Path("{method}")
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> delete(@PathParam("method") TfaMethod method);

  @GET
  @Path("{method}/setup")
  @CookieAuth
  CompletionStage<Response> tfaSetupStart(@PathParam("method") TfaMethod method);

  @POST
  @Path("{method}/setup")
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupComplete(
      @PathParam("method") TfaMethod method,
      @NotNull(message = "Required") @Valid TfaChallengeCompleteDTO body);

  @POST
  @Path("sms/send_code")
  @CookieAuth
  CompletionStage<Response> sendCode();
}
