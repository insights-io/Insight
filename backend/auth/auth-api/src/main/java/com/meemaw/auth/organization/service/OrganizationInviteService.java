package com.meemaw.auth.organization.service;

import com.meemaw.auth.organization.model.dto.TeamInviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteCreateDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface OrganizationInviteService {

  CompletionStage<TeamInviteDTO> createTeamInvite(
      TeamInviteCreateDTO invite, AuthUser creator, String acceptInviteURL);

  CompletionStage<AuthUser> acceptTeamInvite(UUID token, TeamInviteAcceptDTO invite);

  CompletionStage<TeamInviteDTO> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL);

  CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal);

  CompletionStage<List<TeamInviteDTO>> listTeamInvites(InsightPrincipal principal);
}
