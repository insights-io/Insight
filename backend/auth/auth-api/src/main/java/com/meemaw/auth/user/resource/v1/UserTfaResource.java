package com.meemaw.auth.user.resource.v1;

import com.meemaw.auth.sso.cookie.CookieAuth;
import com.meemaw.auth.sso.model.dto.TfaCompleteDTO;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(UserTfaResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface UserTfaResource {

  String PATH = "/v1/user/tfa";

  @GET
  @CookieAuth
  CompletionStage<Response> get();

  @GET
  @Path("setup")
  @CookieAuth
  CompletionStage<Response> tfaSetupStart();

  @POST
  @Path("setup")
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupComplete(
      @NotNull(message = "Required") @Valid TfaCompleteDTO body);

  @DELETE
  @CookieAuth
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> tfaSetupDisable();
}
