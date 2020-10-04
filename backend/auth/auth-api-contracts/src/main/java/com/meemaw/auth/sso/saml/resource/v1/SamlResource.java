package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.sso.oauth.OAuth2Resource;
import com.meemaw.auth.sso.session.resource.v1.SsoResource;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(SamlResource.PATH)
public interface SamlResource {

  String PATH = SsoResource.PATH + "/saml";
  String TAG = "SAML";

  @GET
  @Path(OAuth2Resource.SIGNIN_PATH)
  @Tag(name = TAG)
  @Operation(summary = "Sign in")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "302",
            description = "Success",
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
      @NotNull(message = "Required") @QueryParam("redirect") URL redirect,
      @NotBlank(message = "Required") @Email @QueryParam("email") String email);

  @POST
  @Path(OAuth2Resource.CALLBACK_PATH)
  @Tag(name = TAG)
  @Operation(summary = "SAML callback")
  CompletionStage<Response> callback(
      @NotBlank(message = "Required") @FormParam("SAMLResponse") String SAMLResponse,
      @NotBlank(message = "Required") @FormParam("RelayState") String RelayState,
      @CookieParam("state") String sessionState);
}
