package com.skytrust.iot;

import com.alibaba.fastjson.JSON;
import com.skytrust.exception.IoTException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class MqttPublisher {

    private final MqttClient mqttClient;

    public MqttPublisher(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void publishCommand(Long deviceId, String command) {
        if (mqttClient == null) {
            throw new IoTException("MQTT client not available - broker may be down");
        }
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("command", command);
        msg.put("timestamp", Instant.now().toEpochMilli());
        publish("skytrust/device/" + deviceId + "/command", JSON.toJSONString(msg));
        log.info("Command [{}] sent to device {}", command, deviceId);
    }

    public void publishTelemetryRequest(Long deviceId) {
        if (mqttClient == null) return;
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("action", "request_telemetry");
        msg.put("timestamp", Instant.now().toEpochMilli());
        publish("skytrust/device/" + deviceId + "/config", JSON.toJSONString(msg));
    }

    private void publish(String topic, String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(false);
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("MQTT publish failed, topic={}: {}", topic, e.getMessage());
            throw new IoTException("Failed to publish MQTT message: " + e.getMessage(), e);
        }
    }
}
