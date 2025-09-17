package com.livk.version;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author livk
 */
@ServletComponentScan
@SpringBootApplication
public class RestFulVersionApp {

	void main(String[] args) {
		SpringApplication.run(RestFulVersionApp.class, args);
	}

}
