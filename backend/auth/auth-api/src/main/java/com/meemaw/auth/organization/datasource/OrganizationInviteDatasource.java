package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import io.vertx.axle.sqlclient.Transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface OrganizationInviteDatasource {

  /**
   * Get an existing team invite by confirmation token.
   *
   * @param token UUID team invite confirmation token
   * @param transaction Transaction context
   * @return maybe team invite
   */
  CompletionStage<Optional<TeamInviteDTO>> get(UUID token, Transaction transaction);

  /**
   * Find team invite with associated organization by confirmation token.
   *
   * @param token UUID confirmation token
   * @return maybe pair of TeamInvite and Organization
   */
  CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> getWithOrganization(UUID token);

  /**
   * Find all team invites associated with an organization.
   *
   * @param organizationId String organization id
   * @return List of team invites
   */
  CompletionStage<List<TeamInviteDTO>> find(String organizationId);

  /**
   * Delete team invite by confirmation token.
   *
   * @param token UUID team invite confirmation token
   * @return Boolean indicating successful deletion
   */
  CompletionStage<Boolean> delete(UUID token);

  /**
   * Delete all team invites associated with an email address in organization. One would call this
   * after team invite has been accepted.
   *
   * @param email String email address
   * @param organizationId String organization id
   * @param transaction Transaction context
   * @return Boolean indicating successful deletion
   */
  CompletionStage<Boolean> deleteAll(String email, String organizationId, Transaction transaction);

  /**
   * Create a new team invite.
   *
   * @param organizationId String organization id
   * @param creatorId UUID creator id
   * @param teamInvite CanInviteSend data relevant to email template
   * @param transaction Transaction context
   * @return newly created InviteDTO
   */
  CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      Transaction transaction);
}
