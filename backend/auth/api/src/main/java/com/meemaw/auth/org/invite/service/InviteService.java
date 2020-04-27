package com.meemaw.auth.org.invite.service;

import com.meemaw.auth.org.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.org.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.org.invite.model.dto.InviteDTO;
import com.meemaw.auth.org.invite.model.dto.InviteSendDTO;
import com.meemaw.shared.auth.InsightPrincipal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface InviteService {

  CompletionStage<InviteDTO> create(InviteCreateDTO inviteCreate, InsightPrincipal principal);

  CompletionStage<Boolean> accept(InviteAcceptDTO teamInviteAccept);

  CompletionStage<Void> send(InviteSendDTO inviteSendDTO, InsightPrincipal principal);

  CompletionStage<Boolean> delete(UUID token, InsightPrincipal principal);

  CompletionStage<List<InviteDTO>> list(InsightPrincipal principal);
}
