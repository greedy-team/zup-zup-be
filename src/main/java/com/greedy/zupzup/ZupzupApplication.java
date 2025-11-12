package com.greedy.zupzup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@EnableJpaAuditing
@SpringBootApplication
public class ZupzupApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZupzupApplication.class, args);
	}

}
