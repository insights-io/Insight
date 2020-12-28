package com.meemaw.session.pages.resource.v1;

import com.meemaw.auth.sso.BearerTokenSecurityScheme;
import com.meemaw.auth.sso.SsoSessionCookieSecurityScheme;
import com.meemaw.session.model.PageVisitCreateParams;
import com.meemaw.session.model.PageVisitDTO;
import com.meemaw.session.model.PageVisitSessionLink;
import com.meemaw.shared.rest.response.CountDataResponse;
import com.meemaw.shared.rest.response.ErrorDataResponse;
import com.rebrowse.api.RebrowseApiDataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
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

@Path(PageVisitResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "session-api")
public interface PageVisitResource {

  String PATH = "/v1/pages";
  String TAG = "Page Visit";

  @POST
  @Tag(name = TAG)
  @Operation(summary = "Create Page Visit")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Page Visit Session Link",
            content =
                @Content(
                    schema = @Schema(implementation = PageVisitSessionLinkDataResponse.class),
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
  CompletionStage<Response> create(
      @NotNull(message = "Required") @Valid PageVisitCreateParams body,
      @NotBlank(message = "Required") @HeaderParam(HttpHeaders.USER_AGENT) String userAgent);

  @GET
  @Path("{id}")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @Tag(name = TAG)
  @Operation(summary = "Retrieve Page Visit")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Page Visit",
            content =
                @Content(
                    schema = @Schema(implementation = PageVisitDataResponse.class),
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
  CompletionStage<Response> retrieve(
      @PathParam("id") UUID id, @QueryParam("organizationId") String organizationId);

  @GET
  @Path("count")
  @Tag(name = TAG)
  @Operation(summary = "Count Page Visits")
  @SecurityRequirements(
      value = {
        @SecurityRequirement(name = BearerTokenSecurityScheme.NAME),
        @SecurityRequirement(name = SsoSessionCookieSecurityScheme.NAME)
      })
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Page Visit Count",
            content =
                @Content(
                    schema = @Schema(implementation = CountDataResponse.class),
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
  CompletionStage<Response> count();

  class PageVisitDataResponse extends RebrowseApiDataResponse<PageVisitDTO> {}

  class PageVisitSessionLinkDataResponse extends RebrowseApiDataResponse<PageVisitSessionLink> {}
}
