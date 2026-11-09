package ru.warmhouse.devices.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.warmhouse.devices.api.ApiException;
import ru.warmhouse.devices.domain.Capability;
import ru.warmhouse.devices.domain.CapabilityRepository;
import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.CommandRepository;
import ru.warmhouse.devices.domain.CommandType;
import ru.warmhouse.devices.domain.Device;
import ru.warmhouse.devices.domain.DeviceRepository;
import ru.warmhouse.devices.domain.DeviceStatus;

import java.util.List;
import java.util.UUID;

@Service
public class CommandService {

    private final DeviceService deviceService;
    private final DeviceRepository devices;
    private final CommandRepository commands;
    private final CapabilityRepository capabilities;
    private final List<CommandSender> senders;

    public CommandService(DeviceService deviceService, DeviceRepository devices, CommandRepository commands,
                          CapabilityRepository capabilities, List<CommandSender> senders) {
        this.deviceService = deviceService;
        this.devices = devices;
        this.commands = commands;
        this.capabilities = capabilities;
        this.senders = senders;
    }

    // Без @Transactional: HTTP-вызов в монолит не должен держать транзакцию БД.
    public Command send(UUID deviceId, CommandType type, Double value) {
        Device device = deviceService.get(deviceId);
        if (device.getStatus() == DeviceStatus.OFFLINE) {
            throw new ApiException(HttpStatus.CONFLICT, "DEVICE_OFFLINE", "Устройство офлайн");
        }

        Capability capability = capabilities.findByTypeAndCommand(device.getType(), type)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "COMMAND_NOT_SUPPORTED",
                        "Тип " + device.getType().getCode() + " не поддерживает " + type));
        if (type == CommandType.SET_VALUE) {
            checkValue(value, capability);
        }

        Command command = commands.save(new Command(deviceId, type, type == CommandType.SET_VALUE ? value : null));
        CommandSender sender = senders.stream()
                .filter(s -> s.supports(device))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Нет подходящего CommandSender"));
        sender.send(device, command);

        devices.save(device);
        return commands.save(command);
    }

    public Command get(UUID deviceId, UUID commandId) {
        deviceService.get(deviceId);
        return commands.findByIdAndDeviceId(commandId, deviceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMAND_NOT_FOUND", "Команда не найдена"));
    }

    private void checkValue(Double value, Capability capability) {
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Для SET_VALUE нужно передать value");
        }
        boolean tooLow = capability.getMinValue() != null && value < capability.getMinValue();
        boolean tooHigh = capability.getMaxValue() != null && value > capability.getMaxValue();
        if (tooLow || tooHigh) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "value должно быть в диапазоне " + capability.getMinValue() + ".." + capability.getMaxValue());
        }
    }
}
