package io.github.capure.voltcore;

import io.github.capure.voltcore.service.VoltSettingsService;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class VoltCoreApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(VoltCoreApplication.class, args);
        context.getBean(VoltSettingsService.class).init();
    }

    @Bean
    public NewTopic testCaseEditEventsTopic() {
        return TopicBuilder
                .name("test_case_edit_events")
                .replicas(1)
                .partitions(1)
                .build();
    }
}
