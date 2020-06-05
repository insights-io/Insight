package com.meemaw.auth.organization.invite.datasource;

import com.meemaw.auth.organization.invite.model.TeamInvite;
import com.meemaw.auth.organization.invite.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.Organization;
import io.vertx.axle.sqlclient.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface InviteDatasource {

  /**
   * Find an existing team invite by confirmation token.
   *
   * @param token UUID team invite confirmation token
   * @param transaction Transaction context
   * @return maybe InviteDTO
   */
  CompletionStage<Optional<TeamInvite>> findTeamInvite(UUID token, Transaction transaction);

  /**
   * Find team invite with associated organization by confirmation token.
   *
   * @param token UUID confirmation token
   * @return maybe pair of TeamInvite and Organization
   */
  CompletionStage<Optional<Pair<TeamInvite, Organization>>> findTeamInviteWithOrganization(
      UUID token);

  /**
   * Find all team invites associated with an organization.
   *
   * @param organizationId String organization id
   * @return List of team invites
   */
  CompletionStage<List<TeamInvite>> findTeamInvites(String organizationId);

  /**
   * Delete team invite by confirmation token.
   *
   * @param token UUID team invite confirmation token
   * @return Boolean indicating successful deletion
   */
  CompletionStage<Boolean> deleteTeamInvite(UUID token);

  /**
   * Delete all team invites associated with an email address in organization. One would call this
   * after team invite has been accepted.
   *
   * @param email String email address
   * @param organizationId String organization id
   * @param transaction Transaction context
   * @return Boolean indicating successful deletion
   */
  CompletionStage<Boolean> deleteTeamInvites(
      String email, String organizationId, Transaction transaction);

  /**
   * Create a new team invite.
   *
   * @param organizationId String organization id
   * @param creatorId UUID creator id
   * @param teamInvite CanInviteSend data relevant to email template
   * @param transaction Transaction context
   * @return newly created InviteDTO
   */
  CompletionStage<TeamInvite> createTeamInvite(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      Transaction transaction);
}
