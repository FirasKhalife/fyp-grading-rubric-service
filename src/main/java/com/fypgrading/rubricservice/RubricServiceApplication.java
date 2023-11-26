package com.fypgrading.rubricservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RubricServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RubricServiceApplication.class, args);
	}

}
