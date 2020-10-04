package com.meemaw.auth.password.resource.v1;

import com.meemaw.auth.password.model.dto.PasswordChangeRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordForgotRequestDTO;
import com.meemaw.auth.password.model.dto.PasswordResetRequestDTO;
import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.Authenticated;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(PasswordResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PasswordResource {

  String PATH = "/v1/password";
  String TAG = "Password";

  @POST
  @Path("forgot")
  @Tag(name = TAG)
  @Operation(summary = "Create password reset request")
  CompletionStage<Response> forgot(
      @NotNull(message = "Required") @Valid PasswordForgotRequestDTO body);

  @POST
  @Path("reset/{token}")
  @Tag(name = TAG)
  @Operation(summary = "Password reset")
  CompletionStage<Response> reset(
      @PathParam("token") UUID token,
      @NotNull(message = "Required") @Valid PasswordResetRequestDTO body);

  @GET
  @Path("reset/{token}/exists")
  @Tag(name = TAG)
  @Operation(summary = "Password reset request exists")
  CompletionStage<Response> resetRequestExists(@PathParam("token") UUID token);

  @POST
  @Path("change")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Change password")
  CompletionStage<Response> change(
      @NotNull(message = "Required") @Valid PasswordChangeRequestDTO body);
}
