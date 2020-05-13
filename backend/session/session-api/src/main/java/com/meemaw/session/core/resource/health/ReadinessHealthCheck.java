package com.meemaw.session.core.resource.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.time.OffsetDateTime;

@Readiness
public class ReadinessHealthCheck implements HealthCheck {

  private static OffsetDateTime readySince;

  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.up("ReadinessHealthCheck");
  }
}
