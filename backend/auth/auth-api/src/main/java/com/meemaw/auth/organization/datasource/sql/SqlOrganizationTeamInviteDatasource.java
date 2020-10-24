package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.CREATOR_ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.EMAIL;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.ORGANIZATION_ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.ROLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.TABLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.TOKEN;

import com.meemaw.auth.organization.datasource.OrganizationTeamInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import com.meemaw.shared.sql.exception.SqlException;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.Table;

@ApplicationScoped
@Slf4j
public class SqlOrganizationTeamInviteDatasource implements OrganizationTeamInviteDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token) {
    return sqlPool.execute(retrieveQuery(token)).thenApply(this::mapTeamInviteIfPresent);
  }

  @Override
  public CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token, SqlTransaction transaction) {
    return transaction.execute(retrieveQuery(token)).thenApply(this::mapTeamInviteIfPresent);
  }

  private Query retrieveQuery(UUID token) {
    return sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
  }

  @Override
  @Traced
  public CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> retrieveWithOrganization(
      UUID token) {
    Table<?> joined =
        TABLE.leftJoin(SqlOrganizationTable.TABLE).on(SqlOrganizationTable.ID.eq(ORGANIZATION_ID));
    Query query = sqlPool.getContext().selectFrom(joined).where(TOKEN.eq(token));

    return sqlPool
        .execute(query)
        .thenApply(
            rows -> {
              if (!rows.iterator().hasNext()) {
                return Optional.empty();
              }
              Row row = rows.iterator().next();
              TeamInviteDTO teamInvite = mapTeamInvite(row);
              Organization organization = SqlOrganizationDatasource.mapOrganization(row);
              return Optional.of(Pair.of(teamInvite, organization));
            });
  }

  @Override
  @Traced
  public CompletionStage<List<TeamInviteDTO>> list(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool
        .execute(query)
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(SqlOrganizationTeamInviteDatasource::mapTeamInvite)
                    .collect(Collectors.toList()));
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(TOKEN.eq(token)).returning(TOKEN);
    return sqlPool.execute(query).thenApply(pgRowSet -> pgRowSet.iterator().hasNext());
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(
      String email, String organizationId, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(EMAIL.eq(email))
            .and(ORGANIZATION_ID.eq(organizationId));

    return transaction.execute(query).thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      SqlTransaction transaction) {
    String email = teamInvite.getRecipientEmail();
    UserRole role = teamInvite.getRecipientRole();

    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(creatorId, email, organizationId, role.getKey())
            .returning(FIELDS);

    return transaction
        .execute(query)
        .thenApply(rows -> mapTeamInvite(rows.iterator().next()))
        .exceptionally(
            throwable -> {
              SqlException exception = (SqlException) throwable.getCause();
              // TODO: instead of conflict we should be checking if team invite is actually active
              // TODO: (24h) for the email
              if (exception.hadConflict()) {
                throw Boom.conflict()
                    .errors(Map.of(EMAIL.getName(), "User with this email is already invited"))
                    .exception();
              }

              throw exception;
            });
  }

  private Optional<TeamInviteDTO> mapTeamInviteIfPresent(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapTeamInvite(rows.iterator().next()));
  }

  public static TeamInviteDTO mapTeamInvite(Row row) {
    return new TeamInviteDTO(
        row.getUUID(TOKEN.getName()),
        row.getString(EMAIL.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        UserRole.fromString(row.getString(ROLE.getName())),
        row.getUUID(CREATOR_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
