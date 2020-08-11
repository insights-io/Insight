package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.model.TFASetupCompleteDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SsoVerificationResource.PATH)
public interface SsoVerificationResource {

  String PATH = SsoResource.PATH + "/verification";

  @GET
  @Path("setup-tfa")
  @CookieAuth
  @Produces(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupStart();

  @POST
  @Path("setup-tfa")
  @CookieAuth
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupComplete(
      @NotNull(message = "Required") @Valid TFASetupCompleteDTO body);
}
