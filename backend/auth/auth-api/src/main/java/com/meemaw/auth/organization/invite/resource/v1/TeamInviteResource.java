package com.meemaw.auth.organization.invite.resource.v1;

import com.meemaw.auth.organization.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.invite.model.dto.InviteCreateDTO;
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

@Path(TeamInviteResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface TeamInviteResource {

  String PATH = "/v1/organization/invites";

  @POST
  @CookieAuth
  CompletionStage<Response> createTeamInvite(
      @NotNull(message = "Required") @Valid InviteCreateDTO body);

  @GET
  @CookieAuth
  CompletionStage<Response> listTeamInvites();

  @DELETE
  @CookieAuth
  @Path("{token}")
  CompletionStage<Response> deleteTeamInvite(@PathParam("token") UUID token);

  @POST
  @Path("{token}/accept")
  CompletionStage<Response> acceptTeamInvite(
      @PathParam("token") UUID token, @NotNull(message = "Required") @Valid InviteAcceptDTO body);

  @POST
  @CookieAuth
  @Path("{token}/send")
  CompletionStage<Response> sendTeamInvite(@PathParam("token") UUID token);
}
