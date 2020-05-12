package com.meemaw.auth.org.invite.datasource;

import com.meemaw.auth.org.invite.model.dto.InviteCreateIdentifiedDTO;
import com.meemaw.auth.org.invite.model.dto.InviteDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.pg.PgError;
import com.meemaw.shared.rest.exception.DatabaseException;
import com.meemaw.shared.rest.response.Boom;
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

@ApplicationScoped
@Slf4j
public class PgInviteDatasource implements InviteDatasource {

  @Inject PgPool pgPool;

  private static final String FIND_INVITE_RAW_SQL =
      "SELECT * FROM auth.invite WHERE user_email = $1 AND org = $2 AND token = $3";

  @Override
  public CompletionStage<Optional<InviteDTO>> find(String email, String org, UUID token) {
    return pgPool
        .preparedQuery(FIND_INVITE_RAW_SQL, Tuple.of(email, org, token))
        .thenApply(this::inviteFromRowSet);
  }

  @Override
  public CompletionStage<Optional<InviteDTO>> findTransactional(
      Transaction transaction, String email, String org, UUID token) {
    return transaction
        .preparedQuery(FIND_INVITE_RAW_SQL, Tuple.of(email, org, token))
        .thenApply(this::inviteFromRowSet);
  }

  private static final String FIND_ALL_INVITES_RAW_SQL = "SELECT * FROM auth.invite WHERE org = $1";

  @Override
  public CompletionStage<List<InviteDTO>> findAll(String org) {
    return pgPool
        .preparedQuery(FIND_ALL_INVITES_RAW_SQL, Tuple.of(org))
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(this::inviteFromRow)
                    .collect(Collectors.toList()));
  }

  private static final String DELETE_INVITE_RAW_SQL =
      "DELETE FROM auth.invite WHERE token = $1 AND org = $2";

  @Override
  public CompletionStage<Boolean> delete(UUID token, String org) {
    Tuple values = Tuple.of(token, org);
    return pgPool.preparedQuery(DELETE_INVITE_RAW_SQL, values).thenApply(pgRowSet -> true);
  }

  private static final String DELETE_ALL_INVITES_RAW_SQL =
      "DELETE FROM auth.invite WHERE user_email = $1 AND org = $2";

  @Override
  public CompletionStage<Boolean> deleteAll(Transaction transaction, String email, String org) {
    Tuple values = Tuple.of(email, org);
    return transaction
        .preparedQuery(DELETE_ALL_INVITES_RAW_SQL, values)
        .thenApply(pgRowSet -> true);
  }

  private static final String CREATE_INVITE_RAW_SQL =
      "INSERT INTO auth.invite(creator, user_email, org, role) VALUES($1, $2, $3, $4) RETURNING token, created_at";

  @Override
  public CompletionStage<InviteDTO> create(
      Transaction transaction, InviteCreateIdentifiedDTO teamInvite) {
    UUID creator = teamInvite.getCreator();
    String email = teamInvite.getEmail();
    String org = teamInvite.getOrg();
    UserRole role = teamInvite.getRole();

    Tuple values = Tuple.of(creator, email, org, role.toString());
    return transaction
        .preparedQuery(CREATE_INVITE_RAW_SQL, values)
        .thenApply(
            pgRowSet -> {
              Row row = pgRowSet.iterator().next();
              UUID token = row.getUUID("token");
              OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
              return new InviteDTO(token, email, org, role, creator, createdAt);
            })
        .exceptionally(
            throwable -> {
              Throwable cause = throwable.getCause();
              if (cause instanceof PgException) {
                PgException pgException = (PgException) cause;
                if (pgException.getCode().equals(PgError.UNIQUE_VIOLATION.getCode())) {
                  log.error("User has already been invited user={} org={}", email, org);
                  throw Boom.status(Response.Status.CONFLICT)
                      .message("User has already been invited")
                      .exception();
                }
              }
              log.error(
                  "Failed to create invite user={} org={} creator={} role={}",
                  email,
                  org,
                  creator,
                  role,
                  throwable);
              throw new DatabaseException(throwable);
            });
  }

  private Optional<InviteDTO> inviteFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(inviteFromRow(rowSet.iterator().next()));
  }

  private InviteDTO inviteFromRow(Row row) {
    return new InviteDTO(
        row.getUUID("token"),
        row.getString("user_email"),
        row.getString("org"),
        UserRole.valueOf(row.getString("role")),
        row.getUUID("creator"),
        row.getOffsetDateTime("created_at"));
  }
}
