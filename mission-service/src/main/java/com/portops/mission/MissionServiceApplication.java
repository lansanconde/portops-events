package com.portops.mission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication
@EnableScheduling
public class MissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MissionServiceApplication.class, args);
    }

    /** Horloge injectée : rend le temps testable (jamais Instant.now() en dur dans le domaine). */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
