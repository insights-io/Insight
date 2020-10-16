package com.rebrowse.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CaseFormatTest {

  @Test
  public void test_snake_case_to_camel_case() {
    assertNull(CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, null));
    assertEquals("", CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, ""));
    assertEquals("  ", CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, "  "));
    assertEquals("name", CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, "name"));
    assertEquals("defaultRole", CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, "defaultRole"));
    assertEquals("defaultRole", CaseFormat.SNAKE_CASE.to(CaseFormat.CAMEL_CASE, "default_role"));
  }

  @Test
  public void test_camel_case_to_snake_case() {
    assertNull(CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, null));
    assertEquals("", CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, ""));
    assertEquals("  ", CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, "  "));
    assertEquals("name", CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, "name"));
    assertEquals("default_role", CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, "defaultRole"));
    assertEquals("default_role", CaseFormat.CAMEL_CASE.to(CaseFormat.SNAKE_CASE, "default_role"));
  }
}
