package ru.warmhouse.devices.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.service.CommandSender;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaCommandSender implements CommandSender {

    static final String TOPIC = "device.commands";

    private static final Logger log = LoggerFactory.getLogger(KafkaCommandSender.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public KafkaCommandSender(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    @Override
    public boolean supports(Device device) {
        return device.getLegacySensorId() == null;
    }

    @Override
    public void send(Device device, Command command) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("commandId", command.getId());
        message.put("deviceId", device.getId());
        message.put("houseId", device.getHouseId());
        message.put("type", command.getType());
        message.put("value", command.getValue());
        message.put("source", "user");
        message.put("createdAt", Instant.now());

        try {
            kafka.send(TOPIC, device.getId().toString(), mapper.writeValueAsString(message)).get(5, TimeUnit.SECONDS);
            command.markSent();
        } catch (JsonProcessingException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            log.warn("Не удалось отправить команду {} в Kafka: {}", command.getId(), e.getMessage());
            command.markFailed("Kafka: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            command.markFailed("Kafka: прервано");
        }
    }
}
