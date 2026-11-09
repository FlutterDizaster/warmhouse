package ru.warmhouse.devices.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceTypeRepository extends JpaRepository<DeviceType, UUID> {

    Optional<DeviceType> findByCode(String code);
}
