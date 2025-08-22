package com.livk.grpc.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author livk
 */
@Data
public class DeviceDTO {

	@NotBlank(message = "name is required")
	private String name;

	@NotBlank(message = "mac is required")
	private String mac;

}
