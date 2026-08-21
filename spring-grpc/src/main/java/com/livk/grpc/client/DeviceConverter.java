package com.livk.grpc.client;

import com.livk.grpc.proto.entity.ProtoDevice;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author livk
 */
@Mapper
public interface DeviceConverter {

	DeviceConverter INSTANCE = Mappers.getMapper(DeviceConverter.class);

	ProtoDevice.Device convertProto(DeviceDTO dto);

	DeviceVO convertVO(ProtoDevice.Device device);

}
