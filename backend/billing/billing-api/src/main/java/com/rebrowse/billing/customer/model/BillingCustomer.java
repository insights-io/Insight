package com.rebrowse.billing.customer.model;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BillingCustomer {

  String externalId;
  String internalId;
  OffsetDateTime createdAt;
}
