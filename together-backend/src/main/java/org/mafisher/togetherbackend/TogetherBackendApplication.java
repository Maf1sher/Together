package org.mafisher.togetherbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TogetherBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TogetherBackendApplication.class, args);
    }

}
