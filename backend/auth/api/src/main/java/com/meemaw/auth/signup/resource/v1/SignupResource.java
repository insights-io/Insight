package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.dto.SignupRequestCompleteDTO;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(SignupResource.PATH)
public interface SignupResource {

  String PATH = "v1/signup";

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  CompletionStage<Response> signup(
      @NotBlank(message = "Email is required") @Email @FormParam("email") String email);

  @GET
  @Path("exists")
  CompletionStage<Response> signupExists(
      @NotBlank(message = "email is required") @Email @QueryParam("email") String email,
      @NotBlank(message = "org is required") @QueryParam("org") String org,
      @NotNull(message = "token is required") @QueryParam("token") UUID token);

  @POST
  @Path("complete")
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> signupComplete(
      @NotNull(message = "Payload is required") @Valid SignupRequestCompleteDTO req);

}
