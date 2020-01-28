package com.meemaw.testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import io.vertx.axle.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.eclipse.microprofile.config.ConfigProvider;
import org.testcontainers.containers.PostgreSQLContainer;

import java.net.URI;


public class Postgres {

    private static final String DOCKER_TAG = "postgres:11.6";
    private static final String HOST = "localhost";
    private static final String DATABASE_NAME = "postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    private static final int PORT = 5432;

    public static PostgreSQLContainer testContainer() {
        return new PostgreSQLContainer<>(DOCKER_TAG).withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withExposedPorts(PORT)
                .withCreateContainerCmdModifier(cmd -> {
                    cmd
                            .withHostName(HOST)
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(PORT), new ExposedPort(PORT)));
                });
    }


    public static PgPool client(PostgreSQLContainer container) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(container.getMappedPort(PORT))
                .setHost(HOST)
                .setDatabase(container.getDatabaseName())
                .setUser(container.getUsername())
                .setPassword(container.getPassword());

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        return PgPool.pool(connectOptions, poolOptions);
    }

}
