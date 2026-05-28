package com.poultryprophet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableScheduling
public class PoultryProphetApplication {

    public static void main(String[] args) {
        // Allow Hibernate's ByteBuddy to run on JDKs newer than it officially targets
        // (e.g. JDK 25). Must be set before the persistence layer initialises.
        System.setProperty("net.bytebuddy.experimental", "true");
        SpringApplication.run(PoultryProphetApplication.class, args);
    }
}
