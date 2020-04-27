package com.meemaw.auth.password.model;

import com.meemaw.auth.shared.CanExpire;
import io.vertx.axle.sqlclient.Row;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PasswordResetRequest implements CanExpire {

  UUID token;
  UUID userId;
  String email;
  String org;
  OffsetDateTime createdAt;

  public static PasswordResetRequest fromSqlRow(Row row) {
    return new PasswordResetRequest(
        row.getUUID("token"),
        row.getUUID("user_id"),
        row.getString("email"),
        row.getString("org"),
        row.getOffsetDateTime("created_at")
    );
  }

}
