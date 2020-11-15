package com.rebrowse.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.api.query.TermCondition;
import org.junit.jupiter.api.Test;

public class TermConditionTest {

  @Test
  public void term_operation__get_key() {
    assertEquals("eq", TermCondition.EQ.getKey());
    assertEquals("gt", TermCondition.GT.getKey());
    assertEquals("gte", TermCondition.GTE.getKey());
    assertEquals("lt", TermCondition.LT.getKey());
    assertEquals("lte", TermCondition.LTE.getKey());
  }
}
