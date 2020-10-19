package com.meemaw.auth.organization.datasource.sql;

import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.AUTO_GENERATED_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.CREATED_AT;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.CREATOR_ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.EMAIL;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.INSERT_FIELDS;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.ORGANIZATION_ID;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.ROLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.TABLE;
import static com.meemaw.auth.organization.datasource.sql.SqlOrganizationInviteTable.TOKEN;

import com.meemaw.auth.organization.datasource.OrganizationInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.time.OffsetDateTime;
import java.util.List;
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
public class SqlOrganizationInviteDatasource implements OrganizationInviteDatasource {

  @Inject SqlPool sqlPool;

  @Override
  @Traced
  public CompletionStage<Optional<TeamInviteDTO>> get(UUID token, SqlTransaction transaction) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(TOKEN.eq(token));
    return transaction.execute(query).thenApply(this::mapTeamInviteIfPresent);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Pair<TeamInviteDTO, Organization>>> getWithOrganization(
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
  public CompletionStage<List<TeamInviteDTO>> find(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool
        .execute(query)
        .thenApply(
            pgRowSet ->
                StreamSupport.stream(pgRowSet.spliterator(), false)
                    .map(SqlOrganizationInviteDatasource::mapTeamInvite)
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
  public CompletionStage<Boolean> deleteAll(
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
            .returning(AUTO_GENERATED_FIELDS);

    return transaction
        .execute(query)
        .thenApply(
            rows -> {
              Row row = rows.iterator().next();
              UUID token = row.getUUID(TOKEN.getName());
              OffsetDateTime createdAt = row.getOffsetDateTime(CREATED_AT.getName());
              return new TeamInviteDTO(token, email, organizationId, role, creatorId, createdAt);
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
