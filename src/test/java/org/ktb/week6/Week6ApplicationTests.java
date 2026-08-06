package org.ktb.week6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-for-jwt-that-is-long-enough-256-bits"
})
class Week6ApplicationTests {

    @Test
    void contextLoads() {
    }

}
