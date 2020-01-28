package com.meemaw.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SameJSON extends TypeSafeDiagnosingMatcher<String> {

    private String expected;
    private JSONCompareMode compareMode;

    public SameJSON(String expected, JSONCompareMode compareMode) {
        this.expected = expected;
        this.compareMode = compareMode;
    }

    @Override
    protected boolean matchesSafely(String actual, Description mismatchDescription) {
        try {
            JSONAssert.assertEquals(new JSONObject(actual), new JSONObject(this.expected), this.compareMode);
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

    public static Matcher sameJson(String expected) {
        return sameJson(expected, JSONCompareMode.STRICT);
    }

    public static Matcher sameJson(String expected, JSONCompareMode compareMode) {
        return new SameJSON(expected, compareMode);
    }

}
