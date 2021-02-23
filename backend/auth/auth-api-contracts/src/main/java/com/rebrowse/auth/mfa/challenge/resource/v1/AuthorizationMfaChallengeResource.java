package com.rebrowse.auth.mfa.challenge.resource.v1;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.accounts.model.challenge.MfaChallengeResponseDTO;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.rebrowse.auth.mfa.setup.resource.v1.MfaSetupResource;
import com.rebrowse.auth.sso.MfaChallengeSessionCookieSecurityScheme;
import com.rebrowse.shared.rest.response.ErrorDataResponse;
import com.rebrowse.api.RebrowseApiDataResponse;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(AuthorizationMfaChallengeResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface AuthorizationMfaChallengeResource {

  String PATH = "/v1/authorization/challenge/mfa";
  String TAG = "MFA Authorization Challenge";

  @GET
  @Path("{challengeId}")
  @Tag(name = TAG)
  @Operation(summary = "Retrieve Multi-Factor-Authentication challenge")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = MfaChallengeResponseData.class),
                    mediaType = MediaType.APPLICATION_JSON)),
      })
  CompletionStage<Response> retrieve(@PathParam("challengeId") String challengeId);

  @POST
  @Path("{method}/setup")
  @Tag(name = TAG)
  @Operation(summary = "Start Multi-factor authentication configuration setup")
  @SecurityRequirements(
      value = {@SecurityRequirement(name = MfaChallengeSessionCookieSecurityScheme.NAME)})
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Multi-factor authentication setup details",
            content = {
              @Content(
                  schema =
                      @Schema(
                          implementation = MfaSetupResource.MfaTotpSetupStartDataResponse.class),
                  mediaType = MediaType.APPLICATION_JSON),
              @Content(
                  schema =
                      @Schema(
                          implementation =
                              MfaSetupResource.MfaChallengeCodeDetailsDataResponse.class),
                  mediaType = MediaType.APPLICATION_JSON)
            }),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.UNAUTHORIZED_EXAMPLE)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.NOT_FOUND_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> setup(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Authorization challenge id")
          @NotBlank(message = "Required")
          @CookieParam(AuthorizationMfaChallengeSession.COOKIE_NAME)
          String challengeId,
      @PathParam("method") MfaMethod method);

  @POST
  @Path("{method}")
  @Tag(name = TAG)
  @Operation(summary = "Complete Multi-Factor-Authentication challenge")
  @SecurityRequirements(
      value = {@SecurityRequirement(name = MfaChallengeSessionCookieSecurityScheme.NAME)})
  CompletionStage<Response> complete(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Authorization challenge id")
          @NotBlank(message = "Required")
          @CookieParam(AuthorizationMfaChallengeSession.COOKIE_NAME)
          String challengeId,
      @PathParam("method") MfaMethod method,
      @NotNull(message = "Required") @Valid MfaChallengeCompleteDTO body);

  @POST
  @Path("{method}/complete-enforced")
  @Tag(name = TAG)
  @Operation(
      summary = "Complete enforced Multi-Factor-Authentication challenge",
      description =
          "Organization might enforce MFA for its users. In that cause user without MFA setup still needs to be able to complete the challenge by setting MFA on the go.")
  @SecurityRequirements(
      value = {@SecurityRequirement(name = MfaChallengeSessionCookieSecurityScheme.NAME)})
  CompletionStage<Response> completeEnforced(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Authorization challenge ID")
          @NotBlank(message = "Required")
          @CookieParam(AuthorizationMfaChallengeSession.COOKIE_NAME)
          String challengeId,
      @PathParam("method") MfaMethod method,
      @NotNull(message = "Required") @Valid MfaChallengeCompleteDTO body);

  @GET
  @Path("sms/send_code")
  @Tag(name = TAG)
  @Operation(
      summary = "Send SMS Multi-Factor-Authentication challenge code",
      description =
          "Send SMS Multi-Factor-Authentication challenge code to user's phone number. Code is valid for a limited amount of time & can only be sent once per 60 seconds.")
  @SecurityRequirements(
      value = {@SecurityRequirement(name = MfaChallengeSessionCookieSecurityScheme.NAME)})
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Challenge code details",
            content =
                @Content(
                    schema = @Schema(implementation = MfaChallengeCodeResponseData.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "404",
            description = "Not Found",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.NOT_FOUND_EXAMPLE),
            headers = {
              @Header(
                  name = "Set-Cookie",
                  description = "Clear challenge cookie",
                  schema = @Schema(implementation = String.class))
            }),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> sendSmsCode(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              description = "Authorization challenge ID")
          @NotBlank(message = "Required")
          @CookieParam(AuthorizationMfaChallengeSession.COOKIE_NAME)
          String challengeId);

  class AuthorizationMfaChallengeResponseData
      extends RebrowseApiDataResponse<AuthorizationMfaChallengeResponseDTO> {}

  class MfaChallengeResponseData extends RebrowseApiDataResponse<MfaChallengeResponseDTO> {}

  class MfaChallengeCodeResponseData extends RebrowseApiDataResponse<MfaChallengeCodeDetailsDTO> {}
}
