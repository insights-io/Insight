package com.meemaw.search.core.resource.health;

import java.time.OffsetDateTime;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
public class ReadinessHealthCheck implements HealthCheck {

  private static OffsetDateTime readySince;

  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.up("ReadinessHealthCheck");
  }
}
