package com.rebrowse.auth.sso.token.resource.v1;

import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.sso.BearerTokenSecurityScheme;
import com.rebrowse.auth.sso.SsoSessionCookieSecurityScheme;
import com.rebrowse.auth.sso.session.resource.v1.SsoSessionResource;
import com.rebrowse.auth.sso.token.model.dto.AuthTokenDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.shared.rest.response.ErrorDataResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(AuthTokenResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface AuthTokenResource {

  String PATH = SsoSessionResource.PATH + "/auth/token";
  String TAG = "Auth Token";

  @GET
  @Path("user")
  @Tag(name = TAG)
  @Operation(summary = "Retrieve authenticated user")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "User object",
            content =
                @Content(
                    schema = @Schema(implementation = UserDataResponse.class),
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
  CompletionStage<Response> me();

  @GET
  @Tag(name = TAG)
  @Operation(summary = "List auth tokens")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Auth Token collection",
            content =
                @Content(
                    schema = @Schema(implementation = AuthTokenCollectionDataResponse.class),
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

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create auth token")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Auth Token",
            content =
                @Content(
                    schema = @Schema(implementation = AuthTokenDataResponse.class),
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
  CompletionStage<Response> create();

  @DELETE
  @Path("{token}")
  @Tag(name = TAG)
  @Operation(summary = "Delete auth token")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
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
  CompletionStage<Response> delete(@PathParam("token") String token);

  class UserDataResponse extends RebrowseApiDataResponse<UserDTO> {}

  class AuthTokenDataResponse extends RebrowseApiDataResponse<AuthTokenDTO> {}

  class AuthTokenCollectionDataResponse extends RebrowseApiDataResponse<List<AuthTokenDTO>> {}
}
