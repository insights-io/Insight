package com.rebrowse.test.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProjectUtils {

  private static final String BACKEND = "backend";
  private static final String INFRASTRUCTURE = "infrastructure";

  private ProjectUtils() {}

  private static String getUserDirectory() {
    return System.getProperty("user.dir");
  }

  private static File root() {
    return new File(getUserDirectory().split(BACKEND)[0]);
  }

  private static Path backendPath() {
    return Paths.get(root().toString(), BACKEND).toAbsolutePath();
  }

  private static Path infrastructurePath() {
    return Paths.get(root().toString(), INFRASTRUCTURE).toAbsolutePath();
  }

  public static Path getFromInfrastructure(String... args) {
    return Paths.get(infrastructurePath().toString(), args);
  }

  public static Path getFromBackend(String... args) {
    return Paths.get(backendPath().toString(), args);
  }

  public static Path getFromModule(String... args) {
    return Paths.get(getUserDirectory(), args);
  }
}
