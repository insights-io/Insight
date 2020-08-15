package com.meemaw.auth.sso.resource.v1;

import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SsoVerificationResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SsoVerificationResource {

  String PATH = SsoResource.PATH + "/verification";

  @POST
  @Path("complete-tfa")
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> complete(
      @NotBlank(message = "Required") @CookieParam(SsoVerification.COOKIE_NAME)
          String verificationId,
      @NotNull(message = "Required") @Valid TfaCompleteDTO body);

  @GET
  CompletionStage<Response> get(@NotNull(message = "Required") @QueryParam("id") String id);
}
