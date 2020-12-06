package com.meemaw.test.testconainers.api;

import com.meemaw.test.project.ProjectUtils;
import com.meemaw.test.testconainers.api.auth.AuthApiTestExtension;
import com.meemaw.test.testconainers.elasticsearch.ElasticsearchTestExtension;
import com.meemaw.test.testconainers.kafka.KafkaTestExtension;
import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.testcontainers.containers.GenericContainer;

public enum Api {
  SESSION {
    @Override
    public Collection<GenericContainer<?>> dependencies() {
      return List.of(
          ElasticsearchTestExtension.getInstance(),
          KafkaTestExtension.getInstance(),
          PostgresTestExtension.getInstance(),
          AuthApiTestExtension.getInstance());
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

  public Path pathToPostgresMigrations() {
    return ProjectUtils.getFromBackend(name().toLowerCase(), fullName(), "migrations", "postgres");
  }

  public String fullName() {
    return String.format("%s-api", name().toLowerCase());
  }

  public abstract Collection<GenericContainer<?>> dependencies();
}
