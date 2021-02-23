package com.rebrowse.shared.date;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilsTest {

  @Test
  public void getStartOfPeriod__should_handle_different_period() {
    OffsetDateTime start = OffsetDateTime.parse("2020-09-19T09:24:27.736370+02:00");
    Assertions.assertEquals(start, DateUtils.getStartOfPeriod(start, start.plusDays(0), 30));
    Assertions.assertEquals(start, DateUtils.getStartOfPeriod(start, start.plusDays(15), 30));
    Assertions.assertEquals(start, DateUtils.getStartOfPeriod(start, start.plusDays(29), 30));
    Assertions.assertEquals(
        start.plusDays(30), DateUtils.getStartOfPeriod(start, start.plusDays(30), 30));
    Assertions.assertEquals(
        start.plusDays(30), DateUtils.getStartOfPeriod(start, start.plusDays(59), 30));
    Assertions.assertEquals(
        start.plusDays(60), DateUtils.getStartOfPeriod(start, start.plusDays(62), 30));
  }
}
