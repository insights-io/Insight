package com.rebrowse.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rebrowse.Rebrowse;
import com.rebrowse.exception.JsonException;
import java.net.http.HttpHeaders;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public abstract class RawHttpClient {

  /** Maximum sleep time between tries to send HTTP requests after network failure. */
  public static final Duration maxNetworkRetriesDelay = Duration.ofSeconds(5);

  /** Minimum sleep time between tries to send HTTP requests after network failure. */
  public static final Duration minNetworkRetriesDelay = Duration.ofMillis(500);

  public abstract CompletionStage<RebrowseResponse> request(RebrowseRequest request);

  public CompletionStage<RebrowseResponse> requestWithRetries(RebrowseRequest request) {
    return requestWithRetries(request, 0);
  }

  private CompletionStage<RebrowseResponse> requestWithRetries(
      RebrowseRequest request, int attempt) {
    return request(request)
        .exceptionallyCompose(
            exception -> {
              CompletionException completionException = (CompletionException) exception;
              if (!isRetryable(attempt, request, null, completionException)) {
                throw completionException;
              }
              return sleepAndRetry(request, attempt + 1, null);
            })
        .thenCompose(
            response -> {
              if (!isRetryable(attempt, request, response, null)) {
                return CompletableFuture.completedStage(response);
              }
              return sleepAndRetry(request, attempt + 1, response);
            });
  }

  private CompletionStage<RebrowseResponse> sleepAndRetry(
      RebrowseRequest request, int attempt, RebrowseResponse response) {
    sleep(attempt, response);
    return requestWithRetries(request, attempt);
  }

  private boolean isRetryable(
      int attempt,
      RebrowseRequest request,
      RebrowseResponse response,
      CompletionException exception) {
    if (attempt >= request.getRequestOptions().getMaxNetworkRetries()) {
      return false;
    }

    if (exception != null) {
      if (exception.getCause() instanceof HttpTimeoutException) {
        return true;
      }
    } else {
      int statusCode = response.getStatusCode();
      if (statusCode >= 200 && statusCode < 400) {
        return false;
      }

      HttpHeaders headers = response.getHeaders();
      String value = headers.firstValue("Rebrwose-Should-Retry").orElse(null);

      if ("true".equals(value)) {
        return true;
      }

      if ("false".equals(value)) {
        return false;
      }

      // Retry on rate limit error
      if (statusCode == 429) {
        return true;
      }

      // Retry on 500, 503, and other internal errors.
      if (statusCode >= 500) {
        return true;
      }
    }

    return true;
  }

  private void sleep(int attempt, RebrowseResponse response) {
    try {
      Thread.sleep(sleepTime(attempt, response).toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private Duration sleepTime(int attempt, RebrowseResponse response) {
    return tryExtractRetryAfter(response).orElseGet(() -> calculateSleepTime(attempt));
  }

  private Optional<Duration> tryExtractRetryAfter(RebrowseResponse response) {
    if (response == null) {
      return Optional.empty();
    }

    try {
      return response
          .getHeaders()
          .firstValue("Retry-After")
          .map(Integer::parseInt)
          .map(Duration::ofMillis);
    } catch (NumberFormatException ex) {
      return Optional.empty();
    }
  }

  private Duration calculateSleepTime(int attempt) {
    Duration delay =
        Duration.ofNanos((long) (minNetworkRetriesDelay.toNanos() * Math.pow(2, attempt - 1)));

    // Do not allow the number to exceed MaxNetworkRetriesDelay
    if (delay.compareTo(maxNetworkRetriesDelay) > 0) {
      delay = maxNetworkRetriesDelay;
    }

    // Apply some jitter by randomizing the value in the range of 75%-100%.
    double jitter = ThreadLocalRandom.current().nextDouble(0.75, 1.0);
    delay = Duration.ofNanos((long) (delay.toNanos() * jitter));

    // But never sleep less than the base sleep seconds.
    if (delay.compareTo(minNetworkRetriesDelay) < 0) {
      delay = minNetworkRetriesDelay;
    }

    return delay;
  }

  public static String userAgentString() {
    return String.format("Rebrowse/v1 JavaBindings/%s", Rebrowse.VERSION);
  }

  public static String clientUserAgentString() {
    String[] propertyNames = {
      "os.name",
      "os.version",
      "os.arch",
      "java.version",
      "java.vendor",
      "java.vm.version",
      "java.vm.vendor"
    };

    Map<String, String> propertyMap = new HashMap<>();
    for (String propertyName : propertyNames) {
      propertyMap.put(propertyName, System.getProperty(propertyName));
    }
    propertyMap.put("lang", "Java");
    propertyMap.put("publisher", "Rebrowse");

    try {
      return ApiResource.OBJECT_MAPPER.writeValueAsString(propertyMap);
    } catch (JsonProcessingException exception) {
      throw new JsonException(exception);
    }
  }
}
