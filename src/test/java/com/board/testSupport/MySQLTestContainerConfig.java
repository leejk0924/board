package com.board.testSupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration
public class MySQLTestContainerConfig {

    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:9.7")
            .withDatabaseName("board")
            .withUsername("application")
            .withPassword("application");

    static {
        MYSQL_CONTAINER.start();
    }
}
