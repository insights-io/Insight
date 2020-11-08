package com.meemaw.shared.config.model;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Data
public class AppConfigBase {

  @ConfigProperty(name = "git.commit.sha")
  String gitCommitSha;
}
