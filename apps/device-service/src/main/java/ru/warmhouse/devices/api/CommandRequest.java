package ru.warmhouse.devices.api;

import jakarta.validation.constraints.NotNull;
import ru.warmhouse.devices.domain.CommandType;

public record CommandRequest(@NotNull CommandType type, Double value) {
}
