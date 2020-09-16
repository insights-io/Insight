package com.meemaw.auth.billing.model;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BillingCustomer {

  String internalId;
  String externalId;
  OffsetDateTime createdAt;
}
