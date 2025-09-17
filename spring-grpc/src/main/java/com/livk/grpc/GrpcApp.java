package com.livk.grpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author livk
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class GrpcApp {

	void main(String[] args) {
		SpringApplication.run(GrpcApp.class, args);
	}

}
