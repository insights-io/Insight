package com.meemaw.auth.organization.datasource;

import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface OrganizationTeamInviteDatasource {

  CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token);

  CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token, SqlTransaction transaction);

  CompletionStage<Optional<TeamInviteDTO>> retrieveValid(String email, SqlTransaction transaction);

  CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> retrieveWithOrganization(UUID token);

  CompletionStage<Collection<TeamInviteDTO>> list(String organizationId, SearchDTO search);

  CompletionStage<Integer> count(String organizationId, SearchDTO search);

  CompletionStage<Boolean> delete(UUID token);

  CompletionStage<Boolean> delete(String email, String organizationId, SqlTransaction transaction);

  CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      SqlTransaction transaction);
}
