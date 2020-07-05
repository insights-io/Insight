package com.meemaw.auth.organization.service;

import com.meemaw.auth.organization.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.InviteCreateDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.sso.model.InsightPrincipal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface OrganizationInviteService {

  CompletionStage<TeamInviteDTO> createTeamInvite(
      InviteCreateDTO invite, InsightPrincipal principal, String acceptInviteURL);

  CompletionStage<Boolean> acceptTeamInvite(UUID token, InviteAcceptDTO invite);

  CompletionStage<Void> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL);

  CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal);

  CompletionStage<List<TeamInviteDTO>> listTeamInvites(InsightPrincipal principal);
}
