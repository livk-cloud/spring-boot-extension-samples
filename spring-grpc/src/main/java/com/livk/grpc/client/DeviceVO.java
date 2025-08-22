package com.livk.grpc.client;

import lombok.Builder;
import lombok.Data;

/**
 * @author livk
 */
@Data
@Builder
public class DeviceVO {

	private String name;

	private String mac;

}
