package com.livk.grpc.server;

import com.google.protobuf.BoolValue;
import com.livk.context.mapstruct.converter.MapstructService;
import com.livk.grpc.proto.DeviceServiceGrpc.DeviceServiceImplBase;
import com.livk.grpc.proto.entity.ProtoDevice;
import com.livk.grpc.proto.entity.ProtoDevice.DeviceQuery;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author livk
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGrpc extends DeviceServiceImplBase {

	private final MapstructService mapstructService;

	private final DeviceRepository deviceRepository;

	@Override
	public void query(DeviceQuery request, StreamObserver<ProtoDevice.Device> responseObserver) {
		log.info("request name:{}", request.getName());
		Device device = deviceRepository.getByName(request.getName());
		if (device != null) {
			ProtoDevice.Device response = mapstructService.convert(device, ProtoDevice.Device.class);
			responseObserver.onNext(response);
		}
		else {
			responseObserver.onNext(null);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void add(ProtoDevice.Device request, StreamObserver<BoolValue> responseObserver) {
		log.info("request name:{} mac:{}", request.getName(), request.getMac());
		if (deviceRepository.existsByName(request.getName())) {
			log.error("device already exists");
			responseObserver.onNext(BoolValue.of(false));
		}
		else {
			val device = mapstructService.convert(request, Device.class);
			deviceRepository.save(device);
			responseObserver.onNext(BoolValue.of(true));
		}
		responseObserver.onCompleted();
	}

}
