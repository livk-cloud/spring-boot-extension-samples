package com.livk.grpc.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author livk
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

	Device getByName(String name);

	Boolean existsByName(String name);

}
