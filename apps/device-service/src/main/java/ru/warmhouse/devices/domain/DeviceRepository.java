package ru.warmhouse.devices.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    boolean existsByLegacySensorId(Integer legacySensorId);
}
