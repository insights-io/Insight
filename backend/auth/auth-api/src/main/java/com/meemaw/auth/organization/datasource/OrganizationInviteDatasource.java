package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface OrganizationInviteDatasource {

  CompletionStage<Optional<TeamInviteDTO>> get(UUID token, SqlTransaction transaction);

  CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> getWithOrganization(UUID token);

  CompletionStage<List<TeamInviteDTO>> find(String organizationId);

  CompletionStage<Boolean> delete(UUID token);

  CompletionStage<Boolean> deleteAll(
      String email, String organizationId, SqlTransaction transaction);

  CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      SqlTransaction transaction);
}
