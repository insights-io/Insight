package com.rebrowse.auth.password.resource.v1;

import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.accounts.model.challenge.PwdChallengeResponseDTO;
import com.rebrowse.auth.mfa.challenge.resource.v1.AuthorizationMfaChallengeResource;
import com.rebrowse.shared.validation.Password;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(AuthorizationPwdChallengeResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface AuthorizationPwdChallengeResource {

  String PATH = "/v1/authorization/challenge/pwd";
  String TAG = "PWD Authorization Challenge";

  @GET
  @Path("{challengeId}")
  @Tag(name = TAG)
  @Operation(summary = "Retrieve password authorization challenge")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = PwdChallengeResponseData.class),
                    mediaType = MediaType.APPLICATION_JSON)),
      })
  CompletionStage<Response> retrieve(@PathParam("challengeId") String challengeId);

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Complete password authorization challenge")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success; MFA challenge",
            content =
                @Content(
                    schema =
                        @Schema(
                            implementation =
                                AuthorizationMfaChallengeResource
                                    .AuthorizationMfaChallengeResponseData.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "302",
            description = "Success; Redirect",
            headers = {
              @Header(
                  name = "Location",
                  description = "Callback URL where user is redirected to",
                  schema =
                      @Schema(implementation = String.class, example = "http://localhost:3000"))
            }),
      })
  CompletionStage<Response> complete(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Challenge ID")
          @NotBlank(message = "Required")
          @CookieParam(AuthorizationPwdChallengeSession.COOKIE_NAME)
          String challengeId,
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              example = "john.doe@gmail.com",
              description = "Email address associated with the Account")
          @NotBlank(message = "Required")
          @Email
          @FormParam("email")
          String email,
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Password associated with an Account")
          @Password
          @FormParam("password")
          String password);

  class PwdChallengeResponseData extends RebrowseApiDataResponse<PwdChallengeResponseDTO> {}
}
