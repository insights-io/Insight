package com.meemaw.search.indexer;

import com.meemaw.events.model.external.UserEvent;
import com.meemaw.events.model.internal.AbstractBrowserEvent;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class BrowserEventElasticsearchBatchProcessor
    extends ElasticsearchBatchProcessor<UserEvent<AbstractBrowserEvent>> {

  public BrowserEventElasticsearchBatchProcessor(RestHighLevelClient client) {
    super(client);
  }

  @Override
  public DocWriteRequest<?> transform(UserEvent<AbstractBrowserEvent> value) {
    Map<String, Object> index = value.index();
    String id = UUID.randomUUID().toString();
    return new IndexRequest(EventIndex.NAME).id(id).source(index);
  }
}
