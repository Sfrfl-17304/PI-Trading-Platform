package com.example.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret",
        "app.jwt.access-token-expiration-ms=900000",
        "app.jwt.refresh-token-expiration-ms=604800000",
        "app.security.issuer=test-issuer",
        "app.kafka.topic=user-events",
        "spring.kafka.bootstrap-servers=localhost:9092",
    }
)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {}
}
