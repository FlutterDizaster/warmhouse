package ru.warmhouse.devices.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.warmhouse.devices.config.AppProperties;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.domain.DeviceRepository;
import ru.warmhouse.devices.domain.DeviceStatus;
import ru.warmhouse.devices.domain.DeviceType;
import ru.warmhouse.devices.domain.DeviceTypeRepository;

import java.io.IOException;

@Component
public class LegacySensorConsumer {

    private static final Logger log = LoggerFactory.getLogger(LegacySensorConsumer.class);

    private final DeviceRepository devices;
    private final DeviceTypeRepository types;
    private final AppProperties props;
    private final ObjectMapper mapper;

    public LegacySensorConsumer(DeviceRepository devices, DeviceTypeRepository types, AppProperties props,
                                ObjectMapper mapper) {
        this.devices = devices;
        this.types = types;
        this.props = props;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "legacy.sensor.registered", groupId = "device-service")
    public void onSensorRegistered(String payload) throws IOException {
        LegacySensorMessage msg = mapper.readValue(payload, LegacySensorMessage.class);

        if (!"temperature".equals(msg.type())) {
            log.info("Датчик {} неизвестного типа '{}', пропускаем", msg.sensorId(), msg.type());
            return;
        }
        if (devices.existsById(msg.deviceId()) || devices.existsByLegacySensorId(msg.sensorId())) {
            log.info("Устройство для датчика {} уже есть", msg.sensorId());
            return;
        }

        DeviceType thermostat = types.findByCode("thermostat").orElseThrow();
        Device device = new Device(msg.deviceId(), thermostat, props.legacyHouseId(), "legacy-" + msg.sensorId(),
                msg.name(), DeviceStatus.ONLINE);
        device.setLegacySensorId(msg.sensorId());
        device.setUnit(msg.unit());
        devices.save(device);
        log.info("Создано устройство {} для датчика {}", device.getId(), msg.sensorId());
    }
}
