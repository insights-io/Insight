package com.meemaw.auth.organization.invite.datasource.pg;

import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.CREATED_AT;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.CREATOR_ID;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.EMAIL;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.ORGANIZATION_ID;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.ROLE;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.TABLE;
import static com.meemaw.auth.organization.invite.datasource.pg.TeamInviteTable.TOKEN;

import com.meemaw.auth.organization.datasource.PgOrganizationDatasource;
import com.meemaw.auth.organization.invite.datasource.InviteDatasource;
import com.meemaw.auth.organization.invite.model.TeamInvite;
import com.meemaw.auth.organization.invite.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.Organization;
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
import org.jooq.Query;
import org.jooq.conf.ParamType;

@ApplicationScoped
@Slf4j
public class PgInviteDatasource implements InviteDatasource {

  @Inject PgPool pgPool;

  private static final String FIND_TEAM_INVITE_WITH_ORGANIZATION_RAW_SQL =
      "SELECT * FROM auth.team_invite LEFT JOIN auth.organization ON auth.team_invite.organization_id = auth.organization.id WHERE token = $1";

  @Override
  public CompletionStage<Optional<TeamInvite>> findTeamInvite(UUID token, Transaction transaction) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(TOKEN.eq(token));

    return transaction
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(this::inviteFromRowSet);
  }

  @Override
  public CompletionStage<Optional<Pair<TeamInvite, Organization>>> findTeamInviteWithOrganization(
      UUID token) {
    return pgPool
        .preparedQuery(FIND_TEAM_INVITE_WITH_ORGANIZATION_RAW_SQL)
        .execute(Tuple.of(token))
        .thenApply(
            pgRowSet -> {
              if (!pgRowSet.iterator().hasNext()) {
                return Optional.empty();
              }
              Row row = pgRowSet.iterator().next();
              TeamInvite teamInvite = mapInviteDTO(row);
              Organization organization = PgOrganizationDatasource.mapOrganization(row);
              return Optional.of(Pair.of(teamInvite, organization));
            });
  }

  @Override
  public CompletionStage<List<TeamInvite>> findTeamInvites(String organizationId) {
    Query query = SQLContext.POSTGRES.selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(PgInviteDatasource::mapInviteDTO)
                    .collect(Collectors.toList()));
  }

  @Override
  public CompletionStage<Boolean> deleteTeamInvite(UUID token) {
    Query query = SQLContext.POSTGRES.deleteFrom(TABLE).where(TOKEN.eq(token));
    return pgPool
        .preparedQuery(query.getSQL(ParamType.NAMED))
        .execute(Tuple.tuple(query.getBindValues()))
        .thenApply(pgRowSet -> true);
  }

  @Override
  public CompletionStage<Boolean> deleteTeamInvites(
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
  public CompletionStage<TeamInvite> createTeamInvite(
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
              return new TeamInvite(token, email, organizationId, role, creatorId, createdAt);
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

  private Optional<TeamInvite> inviteFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapInviteDTO(rowSet.iterator().next()));
  }

  /**
   * Map SQL row to TeamInvite.
   *
   * @param row sql row
   * @return mapped TeamInvite
   */
  public static TeamInvite mapInviteDTO(Row row) {
    return new TeamInvite(
        row.getUUID(TOKEN.getName()),
        row.getString(EMAIL.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        UserRole.valueOf(row.getString(ROLE.getName())),
        row.getUUID(CREATOR_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
