package ru.warmhouse.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_id")
    private DeviceType type;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;

    @Column(name = "is_on", nullable = false)
    private boolean on;

    @Column(name = "current_value")
    private Double currentValue;

    private String unit;

    @Column(name = "state_updated_at")
    private Instant stateUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "legacy_sensor_id")
    private Integer legacySensorId;

    protected Device() {
    }

    public Device(UUID id, DeviceType type, UUID houseId, String serialNumber, String name, DeviceStatus status) {
        this.id = id;
        this.type = type;
        this.houseId = houseId;
        this.serialNumber = serialNumber;
        this.name = name;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public void updateState(boolean on, Double value) {
        this.on = on;
        if (value != null) {
            this.currentValue = value;
        }
        this.stateUpdatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public DeviceType getType() {
        return type;
    }

    public UUID getHouseId() {
        return houseId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public boolean isOn() {
        return on;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getStateUpdatedAt() {
        return stateUpdatedAt;
    }

    public Integer getLegacySensorId() {
        return legacySensorId;
    }

    public void setLegacySensorId(Integer legacySensorId) {
        this.legacySensorId = legacySensorId;
    }
}
