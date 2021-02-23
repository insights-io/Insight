package com.rebrowse.auth.accounts.resource.v1;

import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(AccountsResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface AccountsResource {

  String PATH = "/v1/accounts";
  String TAG = "Accounts";

  @POST
  @Path("choose")
  @Tag(name = TAG)
  @Operation(summary = "Choose an Account")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "204",
            description = "Success",
            headers = {
              @Header(
                  name = "Set-Cookie",
                  description = "Set AuthorizationPwdChallengeSessionId cookie",
                  schema = @Schema(implementation = String.class))
            }),
      })
  CompletionStage<Response> choose(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              example = "john.doe@gmail.com",
              description = "Email address of the Account")
          @NotBlank(message = "Required")
          @Email
          @FormParam("email")
          String email,
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              example = "http://localhost:3000",
              description =
                  "Callback URL where user will return to after a successful authentication")
          @QueryParam("redirect")
          @NotNull(message = "Required")
          URL redirect);
}
