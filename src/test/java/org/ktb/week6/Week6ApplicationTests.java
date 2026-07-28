package org.ktb.week6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-for-jwt-that-is-long-enough-256-bits",
        "spring.datasource.url=jdbc:h2:mem:week6-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class Week6ApplicationTests {

    @Test
    void contextLoads() {
    }

}
