package com.meemaw.billing.customer.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BillingCustomer {

  String externalId;
  String internalId;
  OffsetDateTime createdAt;
}
