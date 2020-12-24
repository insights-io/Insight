package com.meemaw.events.search.indexer;

import com.meemaw.events.stream.EventsStream;
import com.meemaw.shared.elasticsearch.ElasticsearchUtils;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class SearchIndexerRunner {

  public static void main(String[] args) {
    HttpHost[] httpHosts = ElasticsearchUtils.httpHosts();
    String bootstrapServers = KafkaUtils.fromEnvironment();
    String retryQueue = Optional.ofNullable(System.getenv("RETRY_QUEUE")).orElse("events-retry-0");
    String deadLetterQueue =
        Optional.ofNullable(System.getenv("RETRY_QUEUE")).orElse("events-dead-letter-queue");

    log.info("kafkaBootstrapServers={}", bootstrapServers);
    log.info("retryQueue={}", retryQueue);
    log.info("deadLetterQueue={}", deadLetterQueue);
    log.info("elasticsearchHosts={}", Arrays.toString(httpHosts));

    RestHighLevelClient client = ElasticsearchUtils.restClient(httpHosts);
    SearchIndexer searchIndexer =
        new SearchIndexer(EventsStream.ALL, retryQueue, deadLetterQueue, bootstrapServers, client);

    searchIndexer.start();
  }
}
