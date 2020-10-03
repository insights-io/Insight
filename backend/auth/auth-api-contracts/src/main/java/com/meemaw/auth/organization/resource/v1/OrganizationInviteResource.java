package com.meemaw.auth.organization.resource.v1;

import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.sso.cookie.CookieAuth;
import java.util.UUID;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(OrganizationInviteResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface OrganizationInviteResource {

  String PATH = OrganizationResource.PATH + "/invites";
  String TAG = "Organization invite";

  @POST
  @CookieAuth
  @Tag(name = TAG)
  CompletionStage<Response> createTeamInvite(
      @NotNull(message = "Required") @Valid TeamInviteCreateDTO body);

  @GET
  @CookieAuth
  @Tag(name = TAG)
  CompletionStage<Response> listTeamInvites();

  @DELETE
  @CookieAuth
  @Path("{token}")
  @Tag(name = TAG)
  CompletionStage<Response> deleteTeamInvite(@PathParam("token") UUID token);

  @POST
  @Path("{token}/accept")
  @Tag(name = TAG)
  CompletionStage<Response> acceptTeamInvite(
      @PathParam("token") UUID token,
      @NotNull(message = "Required") @Valid TeamInviteAcceptDTO body);

  @POST
  @CookieAuth
  @Path("{token}/send")
  @Tag(name = TAG)
  CompletionStage<Response> sendTeamInvite(@PathParam("token") UUID token);
}
