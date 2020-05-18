package com.meemaw.session.service;

import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import io.quarkus.runtime.StartupEvent;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;

@ApplicationScoped
@Slf4j
public class SessionSearchService {

  private RestHighLevelClient restClient;

  public void init(@Observes StartupEvent event) {
    log.info("Initializing ...");
    restClient = ElasticsearchUtils.restClient();
  }

  public CompletionStage<List<UUID>> search() {
    log.info(restClient.getLowLevelClient().toString());
    return CompletableFuture.completedStage(Collections.emptyList());
  }
}
