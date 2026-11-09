package ru.warmhouse.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "commands")
public class Command {

    @Id
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandType type;

    @Column(name = "value")
    private Double value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status;

    @Column(length = 1000)
    private String error;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected Command() {
    }

    public Command(UUID deviceId, CommandType type, Double value) {
        this.id = UUID.randomUUID();
        this.deviceId = deviceId;
        this.type = type;
        this.value = value;
        this.status = CommandStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void markSent() {
        status = CommandStatus.SENT;
    }

    public void markConfirmed() {
        status = CommandStatus.CONFIRMED;
        confirmedAt = Instant.now();
    }

    public void markFailed(String error) {
        status = CommandStatus.FAILED;
        this.error = error;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public CommandType getType() {
        return type;
    }

    public Double getValue() {
        return value;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }
}
