package com.aditya.youtube_clone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class YoutubeCloneApplication {
    @Value("${spring.data.mongodb.uri}")
    private static String myApplicationProperty;
	public static void main(String[] args) {
        log.info("Starting Youtube Clone Application with DB URL from secrets: {} ...", System.getenv("DB_URL"));
        log.info("The DB url is: {}", myApplicationProperty);
		SpringApplication.run(YoutubeCloneApplication.class, args);
	}

}
