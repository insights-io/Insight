package com.rebrowse.test.testconainers.api.billing;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.rebrowse.test.testconainers.pg.PostgresTestExtension;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/** If test is annotated with {@link io.quarkus.test.junit.QuarkusTest} use {@link BillingApi}. */
@Target({TYPE})
@Retention(RUNTIME)
@ExtendWith(PostgresTestExtension.class)
public @interface BillingApi {}
