package ru.warmhouse.devices.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String monolithUrl, UUID legacyHouseId) {
}
