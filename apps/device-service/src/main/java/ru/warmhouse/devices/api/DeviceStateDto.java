package ru.warmhouse.devices.api;

import ru.warmhouse.devices.domain.Device;

import java.time.Instant;

public record DeviceStateDto(boolean on, Double value, String unit, Instant updatedAt) {

    public static DeviceStateDto of(Device d) {
        return new DeviceStateDto(d.isOn(), d.getCurrentValue(), d.getUnit(), d.getStateUpdatedAt());
    }
}
