package com.livk.grpc;

import com.livk.grpc.proto.DeviceServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author livk
 */
@Slf4j
@EnableScheduling
@ImportGrpcClients(target = "device", types = DeviceServiceGrpc.DeviceServiceBlockingStub.class)
@SpringBootApplication
public class GrpcApp {

	void main(String[] args) {
		SpringApplication.run(GrpcApp.class, args);
	}

}
