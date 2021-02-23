package com.rebrowse.auth.organization.datasource.sql;

import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.CREATED_AT;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.CREATOR_ID;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.EMAIL;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.FIELDS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.FIELD_MAPPINGS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.INSERT_FIELDS;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.ORGANIZATION_ID;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.ROLE;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.TABLE;
import static com.rebrowse.auth.organization.datasource.sql.SqlOrganizationTeamInviteTable.TOKEN;

import com.rebrowse.auth.organization.datasource.OrganizationTeamInviteDatasource;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.organization.model.TeamInviteTemplateData;
import com.rebrowse.auth.organization.model.dto.TeamInviteDTO;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.sql.client.SqlPool;
import com.rebrowse.shared.sql.client.SqlTransaction;
import com.rebrowse.shared.sql.datasource.AbstractSqlDatasource;
import com.rebrowse.shared.sql.rest.query.SQLSearchDTO;
import io.vertx.mutiny.sqlclient.Row;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;

@ApplicationScoped
@Slf4j
public class SqlOrganizationTeamInviteDatasource extends AbstractSqlDatasource<TeamInviteDTO>
    implements OrganizationTeamInviteDatasource {

  @Inject SqlPool sqlPool;

  public static TeamInviteDTO map(Row row) {
    return new TeamInviteDTO(
        row.getUUID(TOKEN.getName()),
        row.getString(EMAIL.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        UserRole.fromString(row.getString(ROLE.getName())),
        row.getUUID(CREATOR_ID.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }

  @Override
  public TeamInviteDTO fromSql(Row row) {
    return SqlOrganizationTeamInviteDatasource.map(row);
  }

  @Override
  public CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token) {
    return sqlPool.execute(retrieveQuery(token)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<TeamInviteDTO>> retrieve(UUID token, SqlTransaction transaction) {
    return transaction.execute(retrieveQuery(token)).thenApply(this::findOne);
  }

  @Override
  public CompletionStage<Optional<TeamInviteDTO>> retrieveValid(
      String email, SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .selectFrom(TABLE)
            .where(
                EMAIL
                    .eq(email)
                    .and(
                        CREATED_AT.gt(
                            OffsetDateTime.now().minusDays(TeamInviteDTO.DAYS_VALIDITY))));

    return transaction.execute(query).thenApply(this::findOne);
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
              TeamInviteDTO teamInvite = map(row);
              Organization organization = SqlOrganizationDatasource.mapOrganization(row);
              return Optional.of(Pair.of(teamInvite, organization));
            });
  }

  @Override
  @Traced
  public CompletionStage<Collection<TeamInviteDTO>> list(String organizationId, SearchDTO search) {
    SelectConditionStep<?> baseQuery =
        searchQuery(
            sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId)),
            search);

    return sqlPool
        .execute(SQLSearchDTO.of(search).query(baseQuery, FIELD_MAPPINGS))
        .thenApply(this::findMany);
  }

  @Override
  public CompletionStage<Integer> count(String organizationId, SearchDTO search) {
    SelectConditionStep<?> baseQuery =
        searchQuery(
            sqlPool
                .getContext()
                .select(DSL.count())
                .from(TABLE)
                .where(ORGANIZATION_ID.eq(organizationId)),
            search);

    return sqlPool
        .execute(SQLSearchDTO.of(search).query(baseQuery, FIELD_MAPPINGS))
        .thenApply(rows -> rows.iterator().next().getInteger(0));
  }

  private SelectConditionStep<?> searchQuery(SelectConditionStep<?> baseQuery, SearchDTO search) {
    if (search.getQuery() != null) {
      return baseQuery.and(EMAIL.containsIgnoreCase(search.getQuery()));
    }

    return baseQuery;
  }

  @Override
  @Traced
  public CompletionStage<Boolean> delete(UUID token) {
    Query query = sqlPool.getContext().deleteFrom(TABLE).where(TOKEN.eq(token)).returning(TOKEN);
    return sqlPool.execute(query).thenApply(this::hasNext);
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

    return transaction.execute(query).thenApply(this::hasNext);
  }

  @Override
  @Traced
  public CompletionStage<TeamInviteDTO> create(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInvite,
      SqlTransaction transaction) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                creatorId,
                teamInvite.getRecipientEmail(),
                organizationId,
                teamInvite.getRecipientRole().getKey())
            .returning(FIELDS);

    return transaction.execute(query).thenApply(this::expectOne);
  }
}
