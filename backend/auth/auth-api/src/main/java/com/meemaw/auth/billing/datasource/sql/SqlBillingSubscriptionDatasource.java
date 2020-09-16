package com.meemaw.auth.billing.datasource.sql;

import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.CREATED_AT;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.CURRENT_PERIOD_ENDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.INSERT_FIELDS;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.ORGANIZATION_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.PRICE_ID;
import static com.meemaw.auth.billing.datasource.sql.SqlBillingSubscriptionTable.TABLE;

import com.meemaw.auth.billing.datasource.BillingSubscriptionDatasource;
import com.meemaw.auth.billing.model.BillingSubscription;
import com.meemaw.auth.billing.model.CreateBillingSubscriptionParams;
import com.meemaw.shared.sql.client.SqlPool;
import io.vertx.mutiny.sqlclient.Row;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;

@ApplicationScoped
public class SqlBillingSubscriptionDatasource implements BillingSubscriptionDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<BillingSubscription> create(CreateBillingSubscriptionParams params) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                params.getId(),
                params.getOrganizationId(),
                params.getPriceId(),
                params.getCurrentPeriodEnd())
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(rows -> mapBillingSubscription(rows.iterator().next()));
  }

  public static BillingSubscription mapBillingSubscription(Row row) {
    return new BillingSubscription(
        row.getString(ID.getName()),
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(PRICE_ID.getName()),
        row.getLong(CURRENT_PERIOD_ENDS.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
