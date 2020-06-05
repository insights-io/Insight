package com.meemaw.auth.organization.invite.service;

import com.meemaw.auth.organization.invite.model.TeamInvite;
import com.meemaw.auth.organization.invite.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.invite.model.dto.InviteCreateDTO;
import com.meemaw.auth.sso.model.InsightPrincipal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface InviteService {

  CompletionStage<TeamInvite> createTeamInvite(
      InviteCreateDTO invite, InsightPrincipal principal, String acceptInviteURL);

  CompletionStage<Boolean> acceptTeamInvite(UUID token, InviteAcceptDTO invite);

  CompletionStage<Void> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL);

  CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal);

  CompletionStage<List<TeamInvite>> listTeamInvites(InsightPrincipal principal);
}
