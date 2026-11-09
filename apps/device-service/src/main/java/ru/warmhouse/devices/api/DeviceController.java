package ru.warmhouse.devices.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.warmhouse.devices.service.CommandService;
import ru.warmhouse.devices.service.DeviceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final CommandService commandService;

    public DeviceController(DeviceService deviceService, CommandService commandService) {
        this.deviceService = deviceService;
        this.commandService = commandService;
    }

    @GetMapping
    public List<DeviceDto> list(@RequestHeader("X-User-Id") UUID userId) {
        return deviceService.findAll().stream().map(DeviceDto::of).toList();
    }

    @GetMapping("/{deviceId}")
    public DeviceDto get(@PathVariable UUID deviceId, @RequestHeader("X-User-Id") UUID userId) {
        return DeviceDto.of(deviceService.get(deviceId));
    }

    @PutMapping("/{deviceId}/state")
    public DeviceStateDto updateState(@PathVariable UUID deviceId, @RequestHeader("X-User-Id") UUID userId,
                                      @Valid @RequestBody StateUpdateRequest request) {
        return DeviceStateDto.of(deviceService.updateState(deviceId, request.on(), request.value()));
    }

    @PostMapping("/{deviceId}/commands")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CommandDto sendCommand(@PathVariable UUID deviceId, @RequestHeader("X-User-Id") UUID userId,
                                  @Valid @RequestBody CommandRequest request) {
        return CommandDto.of(commandService.send(deviceId, request.type(), request.value()));
    }

    @GetMapping("/{deviceId}/commands/{commandId}")
    public CommandDto getCommand(@PathVariable UUID deviceId, @PathVariable UUID commandId,
                                 @RequestHeader("X-User-Id") UUID userId) {
        return CommandDto.of(commandService.get(deviceId, commandId));
    }
}
