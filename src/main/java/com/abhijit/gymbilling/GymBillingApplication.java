package com.abhijit.gymbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // MUST HAVE to enable @Scheduled
public class GymBillingApplication {
	public static void main(String[] args) {
		SpringApplication.run(GymBillingApplication.class, args);
	}
}
