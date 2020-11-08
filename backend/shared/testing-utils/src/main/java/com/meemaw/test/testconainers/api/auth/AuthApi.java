package com.meemaw.test.testconainers.api.auth;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.junit.jupiter.api.extension.ExtendWith;

import com.meemaw.test.testconainers.pg.PostgresTestExtension;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link
 * com.meemaw.test.testconainers.api.auth.AuthApiTestResource}.
 */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(PostgresTestExtension.class)
public @interface AuthApi {}
