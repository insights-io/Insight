package com.meemaw.test.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectUtils {

  private static final String BACKEND = "backend";

  private static String getUserDirectory() {
    return System.getProperty("user.dir");
  }

  private static File root() {
    return new File(getUserDirectory().split(BACKEND)[0]);
  }

  public static Path backendPath() {
    return Paths.get(root().toString(), BACKEND).toAbsolutePath();
  }

  public static Path getFromBackend(String... args) {
    return Paths.get(ProjectUtils.backendPath().toString(), args);
  }

  public static Path getFromModule(String... args) {
    return Paths.get(getUserDirectory(), args);
  }

}
