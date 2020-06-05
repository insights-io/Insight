package com.meemaw.auth.password.resource.v1;

import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(PasswordResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PasswordResource {

  String PATH = "/v1";

  @POST
  @Path("password_forgot")
  CompletionStage<Response> forgotPassword(
      @NotNull(message = "Required") @Valid PasswordForgotRequestDTO body);

  @POST
  @Path("password_reset/{token}")
  CompletionStage<Response> resetPassword(
      @PathParam("token") UUID token,
      @NotNull(message = "Required") @Valid PasswordResetRequestDTO body);

  @GET
  @Path("password_reset/{token}/exists")
  CompletionStage<Response> passwordResetRequestExists(@PathParam("token") UUID token);
}
