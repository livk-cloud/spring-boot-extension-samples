package com.livk.grpc.client;

import com.livk.grpc.proto.entity.ProtoDevice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.core.convert.converter.Converter;

/**
 * @author livk
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceVOConverter extends Converter<ProtoDevice.Device, DeviceVO> {

}
