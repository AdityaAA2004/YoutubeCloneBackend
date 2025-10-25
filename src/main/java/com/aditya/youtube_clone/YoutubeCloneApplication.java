package com.aditya.youtube_clone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class YoutubeCloneApplication {
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
	public static void main(String[] args) {
        log.info("Starting Youtube Clone Application with DB URL from secrets: {} ...", System.getenv("DB_URL"));
		SpringApplication.run(YoutubeCloneApplication.class, args);
	}
    @Bean
    public CommandLineRunner logMongoUri() {
        return args -> {
            log.info("The DB url is: {}", mongoUri);
        };
    }

}
