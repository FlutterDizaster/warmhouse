package ru.warmhouse.devices.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "device_types")
public class DeviceType {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String protocol;

    protected DeviceType() {
    }

    public DeviceType(String code, String name, String protocol) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.protocol = protocol;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }
}
