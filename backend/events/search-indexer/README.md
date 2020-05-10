# Search indexer

Search indexer is Java process responsible for indexing UserEvents to ElasticSearch.
It works by pooling a Kafka topic and batch upload events to ElasticSearch.

https://eng.uber.com/reliable-reprocessing/
