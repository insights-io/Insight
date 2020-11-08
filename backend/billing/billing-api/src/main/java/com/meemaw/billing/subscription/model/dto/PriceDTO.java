package com.meemaw.billing.subscription.model.dto;

import com.stripe.model.Price;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PriceDTO {

  BigDecimal amount;
  String interval;

  public static PriceDTO fromStripe(Price price) {
    return new PriceDTO(price.getUnitAmountDecimal(), price.getRecurring().getInterval());
  }

  public static PriceDTO free() {
    return new PriceDTO(BigDecimal.ZERO, "month");
  }
}
