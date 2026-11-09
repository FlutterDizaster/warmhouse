package ru.warmhouse.devices.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.warmhouse.devices.config.AppProperties;
import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.CommandType;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.service.CommandSender;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;

@Component
public class LegacyHeatingSender implements CommandSender {

    private static final Logger log = LoggerFactory.getLogger(LegacyHeatingSender.class);
    private static final double DEFAULT_VALUE = 20.0;

    private final RestClient client;

    public LegacyHeatingSender(AppProperties props) {
        // HttpURLConnection не умеет PATCH, поэтому java.net.http
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder()
                .baseUrl(props.monolithUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public boolean supports(Device device) {
        return device.getLegacySensorId() != null;
    }

    @Override
    public void send(Device device, Command command) {
        boolean on = command.getType() != CommandType.TURN_OFF;
        double value = command.getType() == CommandType.SET_VALUE ? command.getValue() : currentOrDefault(device);

        try {
            client.patch()
                    .uri("/api/v1/sensors/{id}/value", device.getLegacySensorId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(String.format(Locale.ROOT, "{\"value\":%s,\"status\":\"%s\"}", value, on ? "active" : "inactive"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Монолит отклонил команду {} для датчика {}: {}", command.getId(), device.getLegacySensorId(), e.getMessage());
            command.markFailed(e.getMessage());
            return;
        }

        command.markConfirmed();
        device.updateState(on, command.getType() == CommandType.SET_VALUE ? value : null);
    }

    // Монолит (gin, binding:"required" на float64) не принимает value = 0,
    // поэтому для включения/выключения шлём текущее значение или 20.
    private double currentOrDefault(Device device) {
        Double current = device.getCurrentValue();
        return current == null || current == 0 ? DEFAULT_VALUE : current;
    }
}
