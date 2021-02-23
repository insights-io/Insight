package com.rebrowse.shared.date;

import java.time.Duration;
import java.time.OffsetDateTime;

public final class DateUtils {

  private DateUtils() {}

  public static OffsetDateTime getStartOfPeriod(
      OffsetDateTime start, OffsetDateTime now, int periodLengthDays) {
    Duration duration = Duration.between(start, now);
    long createdDaysAgo = duration.toDays();
    long periodsElapsed = createdDaysAgo / periodLengthDays;
    return start.plus(Duration.ofDays(periodLengthDays).multipliedBy(periodsElapsed));
  }

  public static OffsetDateTime getStartOfCurrentPeriod(OffsetDateTime start, int periodLengthDays) {
    return getStartOfPeriod(start, OffsetDateTime.now(), periodLengthDays);
  }
}
