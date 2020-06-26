package com.meemaw.session.events.service;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.events.index.UserEventIndex;
import com.meemaw.events.model.external.dto.AbstractBrowserEventDTO;
import com.meemaw.events.model.external.dto.UserEventDTO;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.elasticsearch.ElasticSearchDTO;
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
   * @param searchDTO search bean
   * @return list of user events matching the search criteria
   */
  @Traced
  @Timed(name = "searchEvents", description = "A measure of how long it takes to search fot events")
  public CompletionStage<List<AbstractBrowserEventDTO>> search(
      UUID sessionId, String organizationId, SearchDTO searchDTO) {
    SearchRequest searchRequest = prepareSearchRequest(sessionId, organizationId, searchDTO);

    CompletableFuture<List<AbstractBrowserEventDTO>> completableFuture = new CompletableFuture<>();
    restClient.searchAsync(
        searchRequest,
        RequestOptions.DEFAULT,
        new ActionListener<>() {
          @Override
          public void onResponse(SearchResponse searchResponse) {
            completableFuture.complete(
                StreamSupport.stream(searchResponse.getHits().spliterator(), false)
                    .map(
                        searchHit ->
                            objectMapper
                                .convertValue(searchHit.getSourceAsMap(), UserEventDTO.class)
                                .getEvent())
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

  private SearchRequest prepareSearchRequest(
      UUID sessionId, String organizationId, SearchDTO searchDTO) {
    SearchSourceBuilder searchSourceBuilder =
        new SearchSourceBuilder()
            .query(
                boolQuery()
                    .filter(termQuery(UserEventIndex.ORGANIZATION_ID.getName(), organizationId))
                    .filter(termQuery(UserEventIndex.SESSION_ID.getName(), sessionId.toString())));

    ElasticSearchDTO.of(searchDTO).apply(searchSourceBuilder);

    // TODO: sorting doesn't actually work yet (it is always ascending) -- investigate why
    /*
    FieldSortBuilder sort =
        SortBuilders.fieldSort(
                String.join(
                    ".", UserEventIndex.EVENT.getName(), UserEventIndex.EVENT_TIMESTAMP.getName()))
            .order(SortOrder.DESC)
            .setNestedSort(new NestedSortBuilder(UserEventIndex.EVENT.getName()));
     */

    return new SearchRequest(UserEventIndex.NAME).source(searchSourceBuilder);
  }
}
