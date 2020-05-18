package com.meemaw.search.indexer;

import com.meemaw.events.stream.EventsStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class SearchIndexerRunner {

  public static void main(String[] args) {
    HttpHost[] elasticsearchHosts = ElasticsearchUtils.fromEnvironment();
    String bootstrapServers = KafkaUtils.fromEnvironment();
    String retryQueue = Optional.ofNullable(System.getenv("RETRY_QUEUE")).orElse("events-retry-0");
    String deadLetterQueue =
        Optional.ofNullable(System.getenv("RETRY_QUEUE")).orElse("events-dead-letter-queue");

    // TODO: client should be created in SearchIndexer
    RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(elasticsearchHosts));

    log.info("elasticsearchHosts: {}", elasticsearchHosts);
    log.info("kafkaBootstrapServers: {}", bootstrapServers);
    log.info("retryQueue: {}", retryQueue);
    log.info("deadLetterQueue: {}", deadLetterQueue);

    SearchIndexer searchIndexer =
        new SearchIndexer(EventsStream.ALL, retryQueue, deadLetterQueue, bootstrapServers, client);

    searchIndexer.start();
  }
}
