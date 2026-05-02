package com.skytrust.iot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MqttMessageListener implements MqttCallback {

    private static final Pattern TOPIC_DEVICE_ID = Pattern.compile("skytrust/device/(\\d+)/.+");

    private final DeviceWebSocketHandler webSocketHandler;

    public MqttMessageListener(DeviceWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT connection lost: {}", cause != null ? cause.getMessage() : "unknown");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        log.debug("MQTT message arrived, topic={}, qos={}, payload={}", topic, message.getQos(), payload);

        Matcher matcher = TOPIC_DEVICE_ID.matcher(topic);
        if (!matcher.find()) {
            log.warn("Cannot extract deviceId from topic: {}", topic);
            return;
        }
        Long deviceId = Long.parseLong(matcher.group(1));

        try {
            JSONObject data = JSON.parseObject(payload);
            webSocketHandler.broadcastDeviceUpdate(deviceId, data);
        } catch (Exception e) {
            log.error("Failed to parse MQTT payload as JSON, topic={}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // no-op
    }
}
