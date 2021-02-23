package com.rebrowse.auth.core.resource.health;

import com.hazelcast.cluster.ClusterState;
import com.rebrowse.shared.hazelcast.cdi.HazelcastProvider;
import javax.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
public class HazelcastHealthCheck implements HealthCheck {

  @Inject HazelcastProvider hazelcastProvider;

  @Override
  public HealthCheckResponse call() {
    ClusterState state = hazelcastProvider.getInstance().getCluster().getClusterState();
    if (state.equals(ClusterState.ACTIVE)) {
      return HealthCheckResponse.up("Hazelcast");
    } else {
      return HealthCheckResponse.down("Hazelcast");
    }
  }
}
