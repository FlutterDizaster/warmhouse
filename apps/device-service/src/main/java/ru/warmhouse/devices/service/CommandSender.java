package ru.warmhouse.devices.service;

import ru.warmhouse.devices.domain.Command;
import ru.warmhouse.devices.domain.Device;

public interface CommandSender {

    boolean supports(Device device);

    // статус команды (SENT, CONFIRMED, FAILED) выставляет сам sender
    void send(Device device, Command command);
}
