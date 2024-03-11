package io.github.capure.voltcore;

import io.github.capure.voltcore.service.VoltSettingsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VoltCoreApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(VoltCoreApplication.class, args);
        context.getBean(VoltSettingsService.class).init();
    }

}
