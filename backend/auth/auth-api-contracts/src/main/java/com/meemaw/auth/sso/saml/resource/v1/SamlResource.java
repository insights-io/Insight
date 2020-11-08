package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.sso.SsoSignInSession;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.concurrent.CompletionStage;

@Path(SamlResource.PATH)
public interface SamlResource {

  String PATH = SsoSessionResource.PATH + "/saml";
  String SIGNIN_PATH = "signin";
  String CALLBACK_PATH = "callback";
  String TAG = "SAML SSO";

  @GET
  @Path(SIGNIN_PATH)
  @Tag(name = TAG)
  @Operation(summary = "SAML sign in")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "302",
            description = "Redirect to identity provider",
            headers = {
              @Header(name = "Set-Cookie", description = "Set anti-forgery state cookie")
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
  CompletionStage<Response> signIn(
      @Parameter(
              required = true,
              schema = @Schema(implementation = String.class),
              example = "http://localhost:3000",
              description =
                  "Callback URL where user will return to after a successful authentication")
          @NotNull(message = "Required")
          @QueryParam("redirect")
          URL redirect,
      @Parameter(example = "user@example.com", required = true)
          @NotBlank(message = "Required")
          @Email
          @QueryParam("email")
          String email);

  @POST
  @Path(CALLBACK_PATH)
  @Tag(name = TAG)
  @Operation(summary = "SAML callback")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "302",
            description =
                "Redirect user back to the application. If error occurred, it will be injected into the \"oauthError\" query parameter of the URL.",
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
  CompletionStage<Response> callback(
      @NotBlank(message = "Required") @FormParam("SAMLResponse") String SAMLResponse,
      @NotBlank(message = "Required") @FormParam("RelayState") String RelayState,
      @CookieParam(SsoSignInSession.COOKIE_NAME) String sessionState);
}
