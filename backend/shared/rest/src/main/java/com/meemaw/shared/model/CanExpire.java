package com.meemaw.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.time.OffsetDateTime;

public interface CanExpire {

  OffsetDateTime getCreatedAt();

  @JsonIgnore
  default int getDaysValidity() {
    return 1;
  }

  default boolean hasExpired() {
    Instant lastActive = getCreatedAt().plusDays(getDaysValidity()).toInstant();
    return Instant.now().isAfter(lastActive);
  }
}
