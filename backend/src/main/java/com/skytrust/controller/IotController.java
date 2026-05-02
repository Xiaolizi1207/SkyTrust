package com.skytrust.controller;

import com.skytrust.common.Result;
import com.skytrust.iot.MqttPublisher;
import com.skytrust.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class IotController {

    private final MqttPublisher mqttPublisher;
    private final DeviceService deviceService;

    public IotController(MqttPublisher mqttPublisher, DeviceService deviceService) {
        this.mqttPublisher = mqttPublisher;
        this.deviceService = deviceService;
    }

    @PostMapping("/devices/{id}/command")
    public Result<Map<String, Object>> sendCommand(@PathVariable Long id, @RequestParam String command) {
        if (!"hover".equals(command) && !"rtl".equals(command)
                && !"land".equals(command) && !"takeoff".equals(command)) {
            return Result.error("Invalid command. Supported: hover, rtl, land, takeoff");
        }
        mqttPublisher.publishCommand(id, command);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", id);
        result.put("command", command);
        result.put("success", true);
        return Result.success(result, "Command sent");
    }

    @PostMapping("/devices/{id}/telemetry-request")
    public Result<Map<String, Object>> requestTelemetry(@PathVariable Long id) {
        mqttPublisher.publishTelemetryRequest(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", id);
        result.put("action", "request_telemetry");
        result.put("success", true);
        return Result.success(result, "Telemetry request sent");
    }

    @GetMapping("/devices/{id}/status")
    public Result<Map<String, Object>> getDeviceStatus(@PathVariable Long id) {
        var device = deviceService.getById(id);
        if (device == null) {
            return Result.error("Device not found");
        }
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("deviceId", device.getId());
        status.put("deviceName", device.getDeviceName());
        status.put("status", device.getStatus());
        status.put("latitude", device.getLatitude());
        status.put("longitude", device.getLongitude());
        status.put("altitude", device.getAltitude());
        status.put("batteryLevel", device.getBatteryLevel());
        status.put("speed", device.getSpeed());
        status.put("lastOnlineTime", device.getLastOnlineTime());
        return Result.success(status);
    }
}
