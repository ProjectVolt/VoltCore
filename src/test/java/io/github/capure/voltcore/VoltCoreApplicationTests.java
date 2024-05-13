package io.github.capure.voltcore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
@SpringBootTest
class VoltCoreApplicationTests {
    @Test
    void contextLoads() {
    }

}
