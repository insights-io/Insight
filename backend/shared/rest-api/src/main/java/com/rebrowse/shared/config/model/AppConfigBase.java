package com.rebrowse.shared.config.model;

import javax.enterprise.context.ApplicationScoped;
import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Data
public class AppConfigBase {

  @ConfigProperty(name = "git.commit.sha")
  String gitCommitSha;
}
