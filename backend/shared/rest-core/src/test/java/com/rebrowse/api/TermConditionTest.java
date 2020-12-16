package com.rebrowse.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rebrowse.api.query.TermCondition;
import org.junit.jupiter.api.Test;

public class TermConditionTest {

  @Test
  public void term_condition__get_key() {
    assertEquals("eq", TermCondition.EQ.getKey());
    assertEquals("gt", TermCondition.GT.getKey());
    assertEquals("gte", TermCondition.GTE.getKey());
    assertEquals("lt", TermCondition.LT.getKey());
    assertEquals("lte", TermCondition.LTE.getKey());
  }

  @Test
  public void term_operation__rhs() {
    assertEquals("eq:value", TermCondition.EQ.rhs("value"));
    assertEquals("gt:value", TermCondition.GT.rhs("value"));
    assertEquals("gte:value", TermCondition.GTE.rhs("value"));
    assertEquals("lt:value", TermCondition.LT.rhs("value"));
    assertEquals("lte:value", TermCondition.LTE.rhs("value"));
  }
}
