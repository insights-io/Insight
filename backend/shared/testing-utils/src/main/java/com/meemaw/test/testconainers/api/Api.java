package com.meemaw.test.testconainers.api;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.testcontainers.containers.GenericContainer;

public enum Api {
  AUTH {
    @Override
    public Collection<GenericContainer<?>> dependencies() {
      return List.of(PostgresTestExtension.getInstance());
    }
  };

  public String imageName() {
    return String.format("%s-test-api", name().toLowerCase());
  }

  public Path dockerfile() {
    return ProjectUtils.getFromBackend(name().toLowerCase(), "api", "docker", "Dockerfile.jvm");
  }

  public Path migrations() {
    return ProjectUtils.getFromBackend(name().toLowerCase(), "api", "migrations", "sql");
  }

  public abstract Collection<GenericContainer<?>> dependencies();
}
