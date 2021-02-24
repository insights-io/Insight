package com.rebrowse.test.testconainers.kafka;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link KafkaTestResource}
 */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(KafkaTestExtension.class)
public @interface Kafka {}
