package com.capgemini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan( basePackages = {"com.capgemini.entity"} )
public class SpsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpsDemoApplication.class, args);
	}
}
