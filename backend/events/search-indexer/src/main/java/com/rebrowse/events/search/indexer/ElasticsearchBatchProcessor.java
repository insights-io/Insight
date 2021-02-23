package com.rebrowse.events.search.indexer;

import com.rebrowse.events.stream.processor.BatchProcessor;
import com.rebrowse.events.stream.processor.BatchProcessorFailureCallback;
import com.rebrowse.events.stream.processor.ProcessorUnavailableException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;

@Slf4j
public abstract class ElasticsearchBatchProcessor<V> implements BatchProcessor<V> {

  private static final TimeValue FLUSH_INTERVAL = TimeValue.timeValueSeconds(5);
  private static final String INTERNAL_SERVER_ERROR = RestStatus.INTERNAL_SERVER_ERROR.name();
  private static final String SERVICE_UNAVAILABLE = RestStatus.SERVICE_UNAVAILABLE.name();

  private final BulkProcessor processor;
  private final Map<String, V> values;
  private BatchProcessorFailureCallback<V> onFailure;

  public ElasticsearchBatchProcessor(RestHighLevelClient client) {
    this.processor = createProcessor(client);
    this.values = new HashMap<>();
  }

  private BulkProcessor createProcessor(RestHighLevelClient client) {
    return BulkProcessor.builder(
            (request, bulkListener) ->
                client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
            new Listener() {
              @Override
              public void beforeBulk(long executionId, BulkRequest request) {
                log.info(
                    "beforeBulk: executionId={}, numberOfActions={}, estimatedSizeInBytes={}",
                    executionId,
                    request.numberOfActions(),
                    request.estimatedSizeInBytes());
              }

              @Override
              public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                long tookMillis = response.getIngestTookInMillis();
                if (!response.hasFailures()) {
                  log.info(
                      "afterBulk: successfully processed executionId={} after {}ms",
                      executionId,
                      tookMillis);
                }

                Throwable cause = null;
                Collection<V> failures = new LinkedList<>();

                for (BulkItemResponse bulkItemResponse : response) {
                  String id = bulkItemResponse.getId();
                  int itemId = bulkItemResponse.getItemId();
                  if (bulkItemResponse.isFailed()) {
                    failures.add(values.get(id));
                    String errorMessage = bulkItemResponse.getFailure().getMessage();
                    String restResponse = bulkItemResponse.getFailure().getStatus().name();

                    log.error(
                        "Failed Message #{}, REST response:{}; errorMessage:{}",
                        itemId,
                        restResponse,
                        errorMessage);

                    if (SERVICE_UNAVAILABLE.equals(restResponse)
                        || INTERNAL_SERVER_ERROR.equals(restResponse)) {
                      cause = new ProcessorUnavailableException(restResponse);
                    }
                  }
                  values.remove(id);
                }

                if (!failures.isEmpty()) {
                  log.info("onFailure.execute count: {}", failures.size(), cause);
                  onFailure.execute(failures, cause);
                }
              }

              @Override
              public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.info("afterBulk: failed executionId={}", executionId, failure);
                onFailure.execute(values.values(), failure);
                values.clear();
              }
            })
        .setFlushInterval(FLUSH_INTERVAL)
        .build();
  }

  @Override
  public void batch(V value) {
    DocWriteRequest<?> writeRequest = transform(value);
    values.put(writeRequest.id(), value);
    processor.add(writeRequest);
  }

  @Override
  public void shutdown() {
    this.close();
  }

  public abstract DocWriteRequest<?> transform(V value);

  @Override
  public void onFailure(BatchProcessorFailureCallback<V> onFailure) {
    this.onFailure = Objects.requireNonNull(onFailure);
  }

  @Override
  public void close() {
    log.info("Closing ...");
    processor.close();
  }

  @Override
  public void flush() {
    log.info("Flushing ...");
    processor.flush();
  }
}
