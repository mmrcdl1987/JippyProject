package com.jippy.notification;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.kafka.listener.auto-startup=false"
})
@Disabled("Requires live PostgreSQL database connection")
class NotificationApplicationTests {

    @Test
    void contextLoads() {
    }

}
