package com.meemaw.testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresSQLTestContainer extends PostgreSQLContainer<PostgresSQLTestContainer> {

    private static final String DOCKER_TAG = "postgres:11.6";
    private static final String HOST = "localhost";
    private static final String DATABASE_NAME = "postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    private static final int PORT = 5432;


    public PostgresSQLTestContainer() {
        super(DOCKER_TAG);
    }

    public static PostgresSQLTestContainer newInstance() {
        return new PostgresSQLTestContainer()
                .withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withExposedPorts(PORT)
                .withCreateContainerCmdModifier(cmd -> {
                    cmd
                            .withHostName(HOST)
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(PORT), new ExposedPort(PORT)));
                });
    }

    public PgPool client() {
        return PostgresSQLTestContainer.client(this);
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
