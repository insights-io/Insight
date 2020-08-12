package com.meemaw.auth.verification.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.model.TfaClientId;
import com.meemaw.auth.sso.model.TfaCompleteDTO;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SsoVerificationResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SsoVerificationResource {

  String PATH = SsoResource.PATH + "/verification";

  @GET
  @Path("setup-tfa")
  @CookieAuth
  CompletionStage<Response> tfaSetupStart();

  @POST
  @Path("setup-tfa")
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupComplete(
      @NotNull(message = "Required") @Valid TfaCompleteDTO body);

  @POST
  @Path("complete-tfa")
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaComplete(
      @NotBlank(message = "Required") @CookieParam(TfaClientId.COOKIE_NAME) String tfaClientId,
      @NotNull(message = "Required") @Valid TfaCompleteDTO body);
}
