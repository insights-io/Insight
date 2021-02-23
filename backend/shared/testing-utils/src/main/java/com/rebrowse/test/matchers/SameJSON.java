package com.rebrowse.test.matchers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class SameJSON extends TypeSafeDiagnosingMatcher<String> {

  private final String expected;
  private final JSONCompareMode compareMode;

  public SameJSON(String expected, JSONCompareMode compareMode) {
    super();
    this.expected = expected;
    this.compareMode = Objects.requireNonNull(compareMode);
  }

  public static Matcher<String> sameJson(String expected) {
    return sameJson(expected, JSONCompareMode.NON_EXTENSIBLE);
  }

  public static Matcher<String> sameJson(String expected, JSONCompareMode compareMode) {
    return new SameJSON(expected, compareMode);
  }

  public static void assertEquals(String expected, String actual, JSONCompareMode compareMode)
      throws JSONException {
    JSONAssert.assertEquals(new JSONObject(expected), new JSONObject(actual), compareMode);
  }

  public static void assertEquals(String expected, String actual) throws JSONException {
    assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Override
  protected boolean matchesSafely(String actual, Description mismatchDescription) {
    try {
      assertEquals(expected, actual, this.compareMode);
      return true;
    } catch (JSONException e) {
      StringWriter out = new StringWriter();
      e.printStackTrace(new PrintWriter(out));
      mismatchDescription.appendText(out.toString());
      return false;
    } catch (AssertionError e) {
      return false;
    }
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("is ").appendValue(this.expected);
  }
}
