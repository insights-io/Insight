package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.CREATOR_ID;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.EMAIL;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.ORGANIZATION_ID;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.ROLE;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.TABLE;
import static com.meemaw.auth.organization.datasource.sql.OrganizationInviteTable.TOKEN;

import com.meemaw.auth.organization.datasource.OrganizationInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.pg.PgError;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.pgclient.PgException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.Table;

@ApplicationScoped
@Slf4j
public class SqlOrganizationInviteDatasource implements OrganizationInviteDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Optional<TeamInviteDTO>> get(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
    return transaction.query(query).thenApply(this::mapTeamInviteIfPresent);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> getWithOrganization(
      UUID token) {
    Table<?> joined =
        TABLE.leftJoin(OrganizationTable.TABLE).on(OrganizationTable.ID.eq(ORGANIZATION_ID));
    Query query = sqlPool.getContext().selectFrom(joined).where(TOKEN.eq(token));

    return sqlPool
        .query(query)
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
  public CompletionStage<List<TeamInviteDTO>> find(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool
        .query(query)
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(SqlOrganizationInviteDatasource::mapTeamInvite)
                    .collect(Collectors.toList()));
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(TOKEN.eq(token));
    return sqlPool.query(query).thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> deleteAll(
      String email, String organizationId, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(EMAIL.eq(email))
            .and(ORGANIZATION_ID.eq(organizationId));

    return transaction.query(query).thenApply(pgRowSet -> true);
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
            .values(creatorId, email, organizationId, role.toString())
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .query(query)
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID(TOKEN.getName());
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              return new TeamInviteDTO(token, email, organizationId, role, creatorId, createdAt);
            })
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause();
              if (cause instanceof PgException) {
                PgException pgException = (PgException) cause;
                if (pgException.getCode().equals(PgError.UNIQUE_VIOLATION.getCode())) {
                  log.error("User has already been invited user={} org={}", email, organizationId);
                  throw Boom.status(Response.Status.CONFLICT)
                      .message("User has already been invited")
                      .exception();
                }
              }
              log.error(
                  "Failed to create invite user={} org={} creator={} role={}",
                  email,
                  organizationId,
                  creatorId,
                  role,
                  throwable);
              throw new DatabaseException(throwable);
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
        UserRole.valueOf(row.getString(ROLE.getName())),
        row.getUUID(CREATOR_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
