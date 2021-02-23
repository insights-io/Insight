package com.rebrowse.test.testconainers.elasticsearch;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link
 * ElasticsearchTestResource}
 */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(ElasticsearchTestExtension.class)
public @interface Elasticsearch {}
