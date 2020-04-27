package com.meemaw.search.indexer;

import com.meemaw.shared.event.model.AbstractBrowserEvent;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;

@Slf4j
public class BrowserEventElasticsearchBatchProcessor extends
    ElasticsearchBatchProcessor<AbstractBrowserEvent> {

  public BrowserEventElasticsearchBatchProcessor(RestHighLevelClient client) {
    super(client);
  }

  // TODO: id should be sourced in
  @Override
  public DocWriteRequest<?> transform(AbstractBrowserEvent value) {
    Map<String, Object> index = value.index();
    String id = UUID.randomUUID().toString();
    return new IndexRequest(EventIndex.NAME).id(id).source(index);
  }
}
