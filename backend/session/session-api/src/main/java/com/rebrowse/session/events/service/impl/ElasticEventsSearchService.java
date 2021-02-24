package com.rebrowse.session.events.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.events.index.UserEventIndex;
import com.rebrowse.events.model.outgoing.dto.AbstractBrowserEventDTO;
import com.rebrowse.events.model.outgoing.dto.UserEventDTO;
import com.rebrowse.session.events.service.EventsSearchService;
import com.rebrowse.shared.elasticsearch.ElasticsearchUtils;
import com.rebrowse.shared.elasticsearch.rest.query.ElasticSearchDTO;
import com.rebrowse.shared.rest.query.SearchDTO;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
@Slf4j
public class ElasticEventsSearchService implements EventsSearchService {

  @Inject ObjectMapper objectMapper;
  private RestHighLevelClient restClient;

  public void init(@Observes StartupEvent event) {
    log.info("Initializing ...");
    restClient = ElasticsearchUtils.restClient();
  }

  @Override
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
    BoolQueryBuilder boolQueryBuilder =
        boolQuery()
            .filter(termQuery(UserEventIndex.ORGANIZATION_ID.getName(), organizationId))
            .filter(termQuery(UserEventIndex.SESSION_ID.getName(), sessionId.toString()));

    SearchSourceBuilder searchSourceBuilder =
        ElasticSearchDTO.of(searchDTO).apply(boolQueryBuilder);

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
