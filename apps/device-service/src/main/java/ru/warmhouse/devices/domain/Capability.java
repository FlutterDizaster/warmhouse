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

import java.util.UUID;

@Entity
@Table(name = "capabilities")
public class Capability {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id")
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandType command;

    private String unit;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    protected Capability() {
    }

    public Capability(DeviceType type, CommandType command, String unit, Double minValue, Double maxValue) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.command = command;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public CommandType getCommand() {
        return command;
    }

    public String getUnit() {
        return unit;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }
}
