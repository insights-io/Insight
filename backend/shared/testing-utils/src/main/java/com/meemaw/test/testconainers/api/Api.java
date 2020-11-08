package com.meemaw.test.testconainers.api;

import org.testcontainers.containers.GenericContainer;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestExtension;
import com.meemaw.test.testconainers.pg.PostgresTestExtension;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public enum Api {
  SESSION {
    @Override
    public Collection<GenericContainer<?>> dependencies() {
      return List.of(PostgresTestExtension.getInstance(), AuthApiTestExtension.getInstance());
    }
  },
  BILLING {
    @Override
    public Collection<GenericContainer<?>> dependencies() {
      return List.of(PostgresTestExtension.getInstance(), AuthApiTestExtension.getInstance());
    }
  },
  AUTH {
    @Override
    public Collection<GenericContainer<?>> dependencies() {
      return List.of(PostgresTestExtension.getInstance());
    }
  };

  public String imageName() {
    return String.format("%s-test", fullName());
  }

  public Path dockerfile() {
    return ProjectUtils.getFromBackend(
        name().toLowerCase(), fullName(), "docker", "Dockerfile.jvm");
  }

  public Path postgresMigrations() {
    return ProjectUtils.getFromBackend(name().toLowerCase(), fullName(), "migrations", "postgres");
  }

  public String fullName() {
    return String.format("%s-api", name().toLowerCase());
  }

  public abstract Collection<GenericContainer<?>> dependencies();
}
