package com.meemaw.test.testconainers.kafka;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link
 * com.meemaw.test.testconainers.kafka.KafkaTestResource}
 */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(KafkaTestExtension.class)
public @interface Kafka {}
