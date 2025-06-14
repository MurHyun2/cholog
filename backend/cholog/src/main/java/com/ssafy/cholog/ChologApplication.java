package com.ssafy.cholog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class ChologApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChologApplication.class, args);
	}
}
