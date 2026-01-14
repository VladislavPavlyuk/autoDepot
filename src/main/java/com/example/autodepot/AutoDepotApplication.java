package com.example.autodepot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoDepotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoDepotApplication.class, args);
	}

}
