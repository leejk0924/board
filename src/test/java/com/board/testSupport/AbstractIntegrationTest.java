package com.board.testSupport;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("test")
@Tag("integrationTest")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(MySQLTestContainerConfig.class)
public class AbstractIntegrationTest {

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySQLTestContainerConfig.MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MySQLTestContainerConfig.MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MySQLTestContainerConfig.MYSQL_CONTAINER::getPassword);
    }
}
