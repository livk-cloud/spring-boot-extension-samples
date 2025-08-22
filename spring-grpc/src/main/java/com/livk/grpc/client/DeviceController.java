package com.livk.grpc.client;

import com.livk.grpc.proto.DeviceServiceGrpc;
import com.livk.grpc.proto.entity.ProtoDevice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author livk
 */
@Slf4j
@RestController
@RequestMapping("/grpc/device")
@RequiredArgsConstructor
public class DeviceController {

	private final DeviceServiceGrpc.DeviceServiceBlockingStub blockingStub;

	private final ConversionService conversionService;

	@GetMapping
	public HttpEntity<DeviceVO> query(@RequestParam String name) {
		log.info("query param:{}", name);
		var query = ProtoDevice.DeviceQuery.newBuilder().setName(name).build();
		var device = blockingStub.query(query);
		return ResponseEntity.ok(conversionService.convert(device, DeviceVO.class));
	}

	@PostMapping
	public HttpEntity<Boolean> add(@RequestBody @Valid DeviceDTO dto) {
		log.info("{}", dto);
		var device = conversionService.convert(dto, ProtoDevice.Device.class);
		var result = blockingStub.add(device).getValue();
		return ResponseEntity.ok(result);
	}

}
