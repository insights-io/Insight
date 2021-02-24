package com.rebrowse.auth.sso.oauth;

import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.sso.session.resource.v1.SsoSessionResource;
import com.rebrowse.shared.rest.response.ErrorDataResponse;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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

public interface OAuthResource {

  String PATH = SsoSessionResource.PATH + "/oauth2";
  String CALLBACK_PATH = "callback";
  String TAG = "OAuth";
  String SIGNIN_PATH = "signin";

  @GET
  @Path(SIGNIN_PATH)
  @Tag(name = TAG)
  @Operation(summary = "Sign in")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "302",
            description = "Redirect to identity provider",
            headers = {
              @Header(name = "Set-Cookie", description = "Set anti-forgery state cookie")
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
  CompletionStage<Response> signIn(
      @Parameter(
              schema = @Schema(implementation = String.class),
              example = "http://localhost:3000",
              description =
                  "Callback URL where user will return to after a successful authentication")
          @NotNull(message = "Required")
          @QueryParam("redirect")
          URL redirect,
      @Parameter(example = "user@example.com") @Nullable @Email @QueryParam("email") String email);

  @GET
  @Path(CALLBACK_PATH)
  @Tag(name = TAG)
  @Operation(summary = "OAuth callback")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "302",
            description =
                "Redirect user back to the application. If error occurred, it will be injected into the \"oauthError\" query parameter of the URL",
            headers = {
              @Header(name = "Set-Cookie", description = "Set SessionId cookie if success")
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
  CompletionStage<Response> oauth2callback(
      @NotBlank(message = "Required") @QueryParam("code") String code,
      @NotBlank(message = "Required") @QueryParam("state") String state,
      @CookieParam(SsoAuthorizationSession.COOKIE_NAME) String sessionState);
}
