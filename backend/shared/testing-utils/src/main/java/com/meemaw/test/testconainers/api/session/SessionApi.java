package com.meemaw.test.testconainers.api.session;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.meemaw.test.testconainers.pg.PostgresTestExtension;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link
 * com.meemaw.test.testconainers.api.auth.AuthApiTestResource}
 */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(PostgresTestExtension.class)
public @interface SessionApi {}
