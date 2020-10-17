package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SessionCookieSecurityScheme;
import com.meemaw.auth.sso.session.resource.v1.SsoSessionResource;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.dto.SsoSetupDTO;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import com.meemaw.shared.rest.response.OkDataResponse;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

@Path(SsoSetupResource.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface SsoSetupResource {

  String PATH = SsoSessionResource.PATH + "/setup";
  String TAG = "Setup SSO";

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create SSO setup")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = SsoSetupDataResponse.class),
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
  CompletionStage<Response> create(@NotNull(message = "Required") @Valid CreateSsoSetupDTO body);

  @GET
  @Tag(name = TAG)
  @Operation(summary = "Retrieve SSO setup")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema = @Schema(implementation = SsoSetupDataResponse.class),
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
  CompletionStage<Response> get();

  @GET
  @Path("{domain}")
  @Tag(name = TAG)
  @Operation(summary = "Retrieve SSO setup associated with a domain")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Success",
            content =
                @Content(
                    schema =
                        @Schema(
                            oneOf = {
                              SsoSetupExistsDataResponse.class,
                              SsoSetupDoesNotExistDataResponse.class
                            }),
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
  CompletionStage<Response> get(@PathParam("domain") String domain);

  class SsoSetupDataResponse extends OkDataResponse<SsoSetupDTO> {}

  class SsoSetupExistsDataResponse extends OkDataResponse<String> {}

  class SsoSetupDoesNotExistDataResponse extends OkDataResponse<Boolean> {}
}
