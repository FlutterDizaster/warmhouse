package ru.warmhouse.devices.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CapabilityRepository extends JpaRepository<Capability, UUID> {

    Optional<Capability> findByTypeAndCommand(DeviceType type, CommandType command);
}
