package com.rebrowse.auth.sso.session.resource.v1;

import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.user.model.dto.UserDataDTO;
import com.rebrowse.shared.rest.response.ErrorDataResponse;
import java.util.List;
import java.util.concurrent.CompletionStage;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(SsoSessionResource.PATH)
@RegisterRestClient(configKey = "auth-api")
public interface SsoSessionResource {

  String PATH = "/v1/sso";
  String TAG = "Session SSO";

  @POST
  @Path("logout")
  @Tag(name = TAG)
  @Operation(summary = "Logout")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "204",
            description = "Success",
            headers = {@Header(name = "Set-Cookie", description = "Clear SessionId cookie")}),
        @APIResponse(
            responseCode = "404",
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
  CompletionStage<Response> logout(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @POST
  @Path("logout-from-all-devices")
  @Tag(name = TAG)
  @Operation(summary = "Logout from all devices")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "204",
            description = "Success",
            headers = {@Header(name = "Set-Cookie", description = "Clear SessionId cookie")}),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> logoutFromAllDevices(
      @NotBlank(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @GET
  @Path("sessions")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "List sessions associated with authenticated user")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = SessionListResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> listAssociatedSessions(
      @NotNull(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  @GET
  @Path("session/{id}/userdata")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Retrieve session")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = UserDataDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "204",
            description = "Not found",
            headers = {@Header(name = "Set-Cookie", description = "Session not found")}),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> retrieveUserData(@PathParam("id") String sessionId);

  @GET
  @Path("session/userdata")
  @Produces(MediaType.APPLICATION_JSON)
  @Tag(name = TAG)
  @Operation(summary = "Retrieve session associated with authenticated user")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = UserDataDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON)),
        @APIResponse(
            responseCode = "204",
            description = "Not found",
            headers = {@Header(name = "Set-Cookie", description = "Clear SessionId cookie")}),
        @APIResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content =
                @Content(
                    schema = @Schema(implementation = ErrorDataResponse.class),
                    mediaType = MediaType.APPLICATION_JSON,
                    example = ErrorDataResponse.SERVER_ERROR_EXAMPLE)),
      })
  CompletionStage<Response> retrieveUserDataByCookieParam(
      @NotNull(message = "Required") @CookieParam(SsoSession.COOKIE_NAME) String sessionId);

  class UserDataDataResponse extends RebrowseApiDataResponse<UserDataDTO> {}

  class SessionListResponse extends RebrowseApiDataResponse<List<String>> {}
}
