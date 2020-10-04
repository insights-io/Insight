package com.meemaw.auth.sso.token.resource.v1;

import com.meemaw.auth.sso.AuthScheme;
import com.meemaw.auth.sso.Authenticated;
import com.meemaw.auth.sso.bearer.BearerTokenAuth;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.auth.sso.token.model.dto.AuthTokenDTO;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import com.meemaw.shared.rest.response.OkDataResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(AuthTokenResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "auth-api")
public interface AuthTokenResource {

  String PATH = SsoResource.PATH + "/auth/token";
  String TAG = "Auth Token";

  @GET
  @Path("user")
  @BearerTokenAuth
  @Tag(name = TAG)
  @Operation(summary = "Retrieve authenticated user")
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
  CompletionStage<Response> me(@HeaderParam(HttpHeaders.AUTHORIZATION) String bearerToken);

  @GET
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "List auth tokens")
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
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Tag(name = TAG)
  @Operation(summary = "Create auth token")
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
  @Authenticated({AuthScheme.BEARER_TOKEN, AuthScheme.COOKIE})
  @Path("{token}")
  @Tag(name = TAG)
  @Operation(summary = "Delete auth token")
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

  class UserDataResponse extends OkDataResponse<UserDTO> {}

  class AuthTokenDataResponse extends OkDataResponse<AuthTokenDTO> {}

  class AuthTokenCollectionDataResponse extends OkDataResponse<List<AuthTokenDTO>> {}
}