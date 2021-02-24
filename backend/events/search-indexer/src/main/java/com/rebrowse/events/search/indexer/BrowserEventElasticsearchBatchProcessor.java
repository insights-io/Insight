package com.rebrowse.events.search.indexer;

import com.rebrowse.events.index.UserEventIndex;
import com.rebrowse.events.model.incoming.AbstractBrowserEvent;
import com.rebrowse.events.model.incoming.UserEvent;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class BrowserEventElasticsearchBatchProcessor
    extends ElasticsearchBatchProcessor<UserEvent<AbstractBrowserEvent<?>>> {

  public BrowserEventElasticsearchBatchProcessor(RestHighLevelClient client) {
    super(client);
  }

  @Override
  public DocWriteRequest<?> transform(UserEvent<AbstractBrowserEvent<?>> value) {
    Map<String, Object> index = value.index();
    String id = UUID.randomUUID().toString();
    return new IndexRequest(UserEventIndex.NAME).id(id).source(index);
  }
}
