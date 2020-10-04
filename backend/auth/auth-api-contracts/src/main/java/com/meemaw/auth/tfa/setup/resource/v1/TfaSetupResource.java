package com.meemaw.auth.tfa.setup.resource.v1;

import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.Authenticated;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.dto.TfaChallengeCodeDetailsDTO;
import com.meemaw.auth.tfa.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.model.dto.TfaSetupDTO;
import com.meemaw.auth.tfa.totp.model.dto.TfaTotpSetupStartDTO;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import com.meemaw.shared.rest.response.OkDataResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(TfaSetupResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface TfaSetupResource {

  String PATH = "/v1/two-factor-authentication/setup";
  String TAG = "Two-Factor-Authentication setup";

  @GET
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "List Two-Factor-Authentication setups")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Two-Factor-Authentication setups",
            content =
                @Content(
                    schema = @Schema(implementation = TfaSetupCollectionDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.UNAUTHORIZED_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> list();

  @GET
  @Path("{method}")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Retrieve Two-Factor-Authentication setup")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Two-Factor-Authentication setup",
            content =
                @Content(
                    schema = @Schema(implementation = TfaSetupDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
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
  CompletionStage<Response> retrieve(@PathParam("method") TfaMethod method);

  @DELETE
  @Path("{method}")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Delete Two-Factor-Authentication setup")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "204", description = "Success"),
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
  CompletionStage<Response> delete(@PathParam("method") TfaMethod method);

  @POST
  @Path("{method}/start")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Start Two-Factor-Authentication setup")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Two-Factor-Authentication setup details",
            content = {
              @Content(
                  schema = @Schema(implementation = TfaTotpSetupStartDataResponse.class),
                  mediaType = MediaType.APPLICATION_JSON),
              @Content(
                  schema = @Schema(implementation = TfaChallengeCodeDetailsDataResponse.class),
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
  CompletionStage<Response> start(@PathParam("method") TfaMethod method);

  @POST
  @Path("{method}/complete")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Consumes(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Complete Two-Factor-Authentication setup")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Two-Factor-Authentication setup details",
            content =
                @Content(
                    schema = @Schema(implementation = TfaSetupDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.UNAUTHORIZED_EXAMPLE)),
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
      @NotNull(message = "Required") @Valid TfaChallengeCompleteDTO body);

  @POST
  @Path("sms/send_code")
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Send Two-Factor-Authentication setup SMS code")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Two-Factor-Authentication setup details",
            content =
                @Content(
                    schema = @Schema(implementation = TfaChallengeCodeDetailsDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.UNAUTHORIZED_EXAMPLE)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> sendSmsCode();

  class TfaSetupDataResponse extends OkDataResponse<TfaSetupDTO> {}

  class TfaSetupCollectionDataResponse extends OkDataResponse<List<TfaSetupDTO>> {}

  class TfaTotpSetupStartDataResponse extends OkDataResponse<TfaTotpSetupStartDTO> {}

  class TfaChallengeCodeDetailsDataResponse extends OkDataResponse<TfaChallengeCodeDetailsDTO> {}
}
