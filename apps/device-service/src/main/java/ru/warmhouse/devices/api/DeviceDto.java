package ru.warmhouse.devices.api;

import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.domain.DeviceStatus;

import java.util.UUID;

public record DeviceDto(UUID id, UUID houseId, UUID roomId, String type, String serialNumber, String name,
                        DeviceStatus status, DeviceStateDto state) {

    public static DeviceDto of(Device d) {
        return new DeviceDto(d.getId(), d.getHouseId(), d.getRoomId(), d.getType().getCode(), d.getSerialNumber(),
                d.getName(), d.getStatus(), DeviceStateDto.of(d));
    }
}
