package com.meemaw.auth.org.invite.resource.v1;

import com.meemaw.auth.org.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.org.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.org.invite.model.dto.InviteSendDTO;
import com.meemaw.auth.org.invite.service.InviteService;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InviteResourceImpl implements InviteResource {

  @Inject InsightPrincipal principal;

  @Inject InviteService inviteService;

  @Override
  public CompletionStage<Response> create(InviteCreateDTO inviteCreate) {
    return inviteService.create(inviteCreate, principal).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> delete(UUID token) {
    return inviteService.delete(token, principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> list() {
    return inviteService.list(principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> accept(InviteAcceptDTO teamInviteAccept) {
    return inviteService.accept(teamInviteAccept).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> send(InviteSendDTO inviteSend) {
    return inviteService.send(inviteSend, principal).thenApply(x -> DataResponse.ok(true));
  }
}
