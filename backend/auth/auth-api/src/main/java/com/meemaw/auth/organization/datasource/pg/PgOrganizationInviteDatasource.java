package com.meemaw.auth.organization.datasource.pg;

import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.CREATOR_ID;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.EMAIL;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.ORGANIZATION_ID;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.ROLE;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.TABLE;
import static com.meemaw.auth.organization.datasource.pg.OrganizationInviteTable.TOKEN;

import com.meemaw.auth.organization.datasource.OrganizationInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.pg.PgError;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.SQLContext;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Transaction;
import io.vertx.axle.sqlclient.Tuple;
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
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgOrganizationInviteDatasource implements OrganizationInviteDatasource {

  @Inject PgPool pgPool;

  @Override
  @Traced
  public CompletionStage<Optional<TeamInviteDTO>> get(UUID token, Transaction transaction) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(TOKEN.eq(token));
    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::mapTeamInviteIfPresent);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> getWithOrganization(
      UUID token) {
    Table<?> joined =
        TABLE.leftJoin(OrganizationTable.TABLE).on(OrganizationTable.ID.eq(ORGANIZATION_ID));
    Query query = SQLContext.POSTGRES.selectFrom(joined).where(TOKEN.eq(token));

    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(
            pgRowSet -> {
              if (!pgRowSet.iterator().hasNext()) {
                return Optional.empty();
              }
              Row row = pgRowSet.iterator().next();
              TeamInviteDTO teamInvite = mapTeamInvite(row);
              Organization organization = PgOrganizationDatasource.mapOrganization(row);
              return Optional.of(Pair.of(teamInvite, organization));
            });
  }

  @Override
  @Traced
  public CompletionStage<List<TeamInviteDTO>> find(String organizationId) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(PgOrganizationInviteDatasource::mapTeamInvite)
                    .collect(Collectors.toList()));
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token) {
    Query query = SQLContext.POSTGRES.deleteFrom(TABLE).where(TOKEN.eq(token));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<Boolean> deleteAll(
      String email, String organizationId, Transaction transaction) {
    Query query =
        SQLContext.POSTGRES
            .deleteFrom(TABLE)
            .where(EMAIL.eq(email))
            .and(ORGANIZATION_ID.eq(organizationId));

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  @Traced
  public CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      Transaction transaction) {
    String email = teamInvite.getRecipientEmail();
    UserRole role = teamInvite.getRecipientRole();

    Query query =
        SQLContext.POSTGRES
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(creatorId, email, organizationId, role.toString())
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
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

  private Optional<TeamInviteDTO> mapTeamInviteIfPresent(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapTeamInvite(rowSet.iterator().next()));
  }

  /**
   * Map SQL row to TeamInvite.
   *
   * @param row sql row
   * @return mapped TeamInvite
   */
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
