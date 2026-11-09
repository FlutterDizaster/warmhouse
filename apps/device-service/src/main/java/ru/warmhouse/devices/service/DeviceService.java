package ru.warmhouse.devices.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.warmhouse.devices.api.ApiException;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.domain.DeviceRepository;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private final DeviceRepository devices;

    public DeviceService(DeviceRepository devices) {
        this.devices = devices;
    }

    public List<Device> findAll() {
        return devices.findAll();
    }

    public Device get(UUID id) {
        return devices.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", "Устройство не найдено"));
    }

    public Device updateState(UUID id, boolean on, Double value) {
        Device device = get(id);
        device.updateState(on, value);
        return devices.save(device);
    }
}
