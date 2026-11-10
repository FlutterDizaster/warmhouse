package ru.warmhouse.devices.api;

import jakarta.validation.constraints.NotNull;

public record StateUpdateRequest(@NotNull Boolean on, Double value) {
}
