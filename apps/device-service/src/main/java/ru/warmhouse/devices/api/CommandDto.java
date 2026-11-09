package ru.warmhouse.devices.api;

import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.CommandStatus;
import ru.warmhouse.devices.domain.CommandType;

import java.time.Instant;
import java.util.UUID;

public record CommandDto(UUID id, UUID deviceId, CommandType type, Double value, CommandStatus status, String error,
                         Instant createdAt, Instant confirmedAt) {

    public static CommandDto of(Command c) {
        return new CommandDto(c.getId(), c.getDeviceId(), c.getType(), c.getValue(), c.getStatus(), c.getError(),
                c.getCreatedAt(), c.getConfirmedAt());
    }
}
