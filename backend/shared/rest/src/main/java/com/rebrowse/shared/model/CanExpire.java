package com.rebrowse.shared.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.time.OffsetDateTime;

public interface CanExpire {

  OffsetDateTime getCreatedAt();

  int getDaysValidity();

  default OffsetDateTime getExpiresAt() {
    return getCreatedAt().plusDays(getDaysValidity());
  }

  default boolean isValid() {
    return !hasExpired();
  }

  @JsonIgnore
  default boolean hasExpired() {
    return Instant.now().isAfter(getExpiresAt().toInstant());
  }
}
