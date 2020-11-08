package com.meemaw.auth.tfa.challenge.resource.v1;

import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.dto.TfaChallengeCodeDetailsDTO;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import com.meemaw.shared.rest.response.OkDataResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(TfaChallengeResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface TfaChallengeResource {

  String PATH = "/v1/two-factor-authentication/challenge";
  String TAG = "Two-Factor-Authentication challenge";

  @POST
  @Path("{method}/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Complete Two-Factor-Authentication challenge")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "204",
            description = "Success",
            headers = {
              @Header(
                  name = "Set-Cookie",
                  description = "Clear challenge cookie and set session cookie",
                  schema = @Schema(implementation = String.class))
            }),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.BAD_REQUEST_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> complete(
      @PathParam("method") TfaMethod method,
      @NotBlank(message = "Required") @CookieParam(SsoChallenge.COOKIE_NAME) String challengeId,
      @NotNull(message = "Required") @Valid TfaChallengeCompleteDTO body);

  @GET
  @Path("{id}")
  @Tag(name = TAG)
  @Operation(summary = "List supported Two-Factor-Authentication challenge methods")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Supported Two-Factor-Authentication methods",
            content =
                @Content(
                    schema = @Schema(implementation = TfaMethodCollectionDataResponse.class),
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
  CompletionStage<Response> list(@PathParam("id") String id);

  @POST
  @Path("sms/send_code")
  @Tag(name = TAG)
  @Operation(summary = "Send Two-Factor-Authentication SMS challenge code")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Challenge code details",
            content =
                @Content(
                    schema = @Schema(implementation = TfaChallengeCodeDetailsDataResponse.class),
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
  CompletionStage<Response> sendSmsChallengeCode(
      @NotBlank(message = "Required") @CookieParam(SsoChallenge.COOKIE_NAME) String challengeId);

  class TfaMethodCollectionDataResponse extends OkDataResponse<List<TfaMethod>> {}

  class TfaChallengeCodeDetailsDataResponse extends OkDataResponse<TfaChallengeCodeDetailsDTO> {}
}
