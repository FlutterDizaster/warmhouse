package ru.warmhouse.devices.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LegacySensorMessage(Integer sensorId, UUID deviceId, String name, String type, String location,
                                  String unit) {
}
