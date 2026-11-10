package ru.warmhouse.devices.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.warmhouse.devices.domain.Capability;
import ru.warmhouse.devices.domain.CapabilityRepository;
import ru.warmhouse.devices.domain.CommandType;
import ru.warmhouse.devices.domain.DeviceType;
import ru.warmhouse.devices.domain.DeviceTypeRepository;

@Component
public class ReferenceDataInitializer {

    private final DeviceTypeRepository types;
    private final CapabilityRepository capabilities;

    public ReferenceDataInitializer(DeviceTypeRepository types, CapabilityRepository capabilities) {
        this.types = types;
        this.capabilities = capabilities;
    }

    // Делаем в @PostConstruct, а не в CommandLineRunner: Kafka-слушатель стартует раньше раннеров
    // и может получить сообщение до того, как справочник заполнен.
    @PostConstruct
    void init() {
        if (types.findByCode("thermostat").isEmpty()) {
            DeviceType thermostat = types.save(new DeviceType("thermostat", "Модуль отопления с датчиком", "legacy-http"));
            capabilities.save(new Capability(thermostat, CommandType.TURN_ON, null, null, null));
            capabilities.save(new Capability(thermostat, CommandType.TURN_OFF, null, null, null));
            capabilities.save(new Capability(thermostat, CommandType.SET_VALUE, "°C", 5.0, 30.0));
        }
        if (types.findByCode("light_relay").isEmpty()) {
            DeviceType relay = types.save(new DeviceType("light_relay", "Реле освещения", "kafka"));
            capabilities.save(new Capability(relay, CommandType.TURN_ON, null, null, null));
            capabilities.save(new Capability(relay, CommandType.TURN_OFF, null, null, null));
        }
    }
}
