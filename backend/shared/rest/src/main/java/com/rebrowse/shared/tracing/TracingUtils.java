package com.rebrowse.shared.tracing;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

public final class TracingUtils {

  private TracingUtils() {}

  public static Span finishExceptionally(Span span, Throwable throwable) {
    Tags.ERROR.set(span, true);
    span.log(throwable.getMessage());
    span.finish();
    return span;
  }
}
