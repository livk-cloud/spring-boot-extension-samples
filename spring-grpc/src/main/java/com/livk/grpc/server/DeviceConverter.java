package com.livk.grpc.server;

import com.livk.context.mapstruct.converter.Converter;
import com.livk.grpc.proto.entity.ProtoDevice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * @author livk
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceConverter extends Converter<Device, ProtoDevice.Device> {

}
