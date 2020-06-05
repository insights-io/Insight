package com.meemaw.auth.signup.resource.v1;

import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
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

@Path(SignUpResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SignUpResource {

  String PATH = "/v1/signup";

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  CompletionStage<Response> signUp(@NotNull(message = "Required") @Valid SignUpRequestDTO body);

  @GET
  @Path("{token}/valid")
  CompletionStage<Response> signUpRequestValid(@PathParam("token") UUID token);

  @GET
  @Path("{token}/complete")
  CompletionStage<Response> signUpRequestComplete(@PathParam("token") UUID token);
}
