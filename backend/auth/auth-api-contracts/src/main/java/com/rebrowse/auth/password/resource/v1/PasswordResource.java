package com.rebrowse.auth.password.resource.v1;

import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.password.model.dto.PasswordChangeRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordForgotRequestDTO;
import com.rebrowse.auth.password.model.dto.PasswordResetRequestDTO;
import com.rebrowse.auth.sso.BearerTokenSecurityScheme;
import com.rebrowse.auth.sso.SsoSessionCookieSecurityScheme;
import com.rebrowse.shared.rest.response.BooleanDataResponse;
import com.rebrowse.shared.rest.response.ErrorDataResponse;
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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
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
  @APIResponses(
      value = {
        @APIResponse(responseCode = "204", description = "Success"),
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
  CompletionStage<Response> forgot(
      @NotNull(message = "Required") @Valid PasswordForgotRequestDTO body);

  @POST
  @Path("reset/{token}")
  @Tag(name = TAG)
  @Operation(summary = "Password reset")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = RebrowseApiDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
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
  CompletionStage<Response> reset(
      @PathParam("token") UUID token,
      @NotNull(message = "Required") @Valid PasswordResetRequestDTO body);

  @GET
  @Path("reset/{token}/exists")
  @Tag(name = TAG)
  @Operation(summary = "Password reset request exists")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Boolean indicating if the request exists",
            content =
                @Content(
                    schema = @Schema(implementation = BooleanDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> resetRequestExists(@PathParam("token") UUID token);

  @POST
  @Path("change")
  @Tag(name = TAG)
  @Operation(summary = "Change password")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(responseCode = "204", description = "Success"),
        @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.BAD_REQUEST_EXAMPLE)),
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
  CompletionStage<Response> change(
      @NotNull(message = "Required") @Valid PasswordChangeRequestDTO body);
}
