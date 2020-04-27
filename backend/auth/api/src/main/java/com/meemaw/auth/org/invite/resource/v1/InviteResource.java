package com.meemaw.auth.org.invite.resource.v1;

import com.meemaw.auth.org.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.org.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.org.invite.model.dto.InviteSendDTO;
import com.meemaw.shared.auth.CookieAuth;
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

@Path(InviteResource.PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface InviteResource {

  String PATH = "v1/org/invites";

  @POST
  @CookieAuth
  CompletionStage<Response> create(
      @NotNull(message = "Payload is required") @Valid InviteCreateDTO teamInviteCreate);

  @GET
  @CookieAuth
  CompletionStage<Response> list();


  @DELETE
  @CookieAuth
  @Path("{token}")
  CompletionStage<Response> delete(@PathParam("token") UUID token);

  @POST
  @Path("accept")
  CompletionStage<Response> accept(
      @NotNull(message = "Payload is required") @Valid InviteAcceptDTO teamInviteAccept);

  @POST
  @CookieAuth
  @Path("send")
  CompletionStage<Response> send(
      @NotNull(message = "Payload is required") @Valid InviteSendDTO inviteSend);

}
