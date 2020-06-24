package com.meemaw.session.events.service;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.external.dto.UserEvent;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import io.quarkus.runtime.StartupEvent;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
@Slf4j
public class EventsSearchService {

  @Inject ObjectMapper objectMapper;
  private RestHighLevelClient restClient;

  public void init(@Observes StartupEvent event) {
    log.info("Initializing ...");
    restClient = ElasticsearchUtils.restClient();
  }

  /**
   * Search for user events asynchronously.
   *
   * @param sessionId session id
   * @param organizationId organization id
   * @return list of user events matching the search criteria
   */
  @Traced
  @Timed(name = "searchEvents", description = "A measure of how long it takes to search fot events")
  public CompletionStage<List<UserEvent<?>>> search(UUID sessionId, String organizationId) {
    SearchRequest searchRequest = prepareSearchRequest(sessionId, organizationId);
    CompletableFuture<List<UserEvent<?>>> completableFuture = new CompletableFuture<>();
    restClient.searchAsync(
        searchRequest,
        RequestOptions.DEFAULT,
        new ActionListener<>() {
          @Override
          public void onResponse(SearchResponse searchResponse) {
            completableFuture.complete(
                StreamSupport.stream(searchResponse.getHits().spliterator(), false)
                    .map(
                        searchHit -> {
                          UserEvent<?> userEvent =
                              objectMapper.convertValue(
                                  searchHit.getSourceAsMap(), UserEvent.class);
                          return userEvent;
                        })
                    .collect(Collectors.toList()));
          }

          @Override
          public void onFailure(Exception ex) {
            log.error("Something went wrong while searching for events", ex);
            completableFuture.completeExceptionally(ex);
          }
        });

    return completableFuture;
  }

  private SearchRequest prepareSearchRequest(UUID sessionId, String organizationId) {
    SearchRequest searchRequest = new SearchRequest(UserEventIndex.NAME);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(
        boolQuery()
            .filter(termQuery(UserEventIndex.ORGANIZATION_ID.getName(), organizationId))
            .filter(termQuery(UserEventIndex.SESSION_ID.getName(), sessionId.toString())));

    searchRequest.source(searchSourceBuilder);
    return searchRequest;
  }
}
