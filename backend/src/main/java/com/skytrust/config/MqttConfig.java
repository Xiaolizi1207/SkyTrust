package com.skytrust.config;

import com.skytrust.iot.MqttMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "skytrust.iot.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttConfig {

    private final MqttProperties mqttProperties;
    private final MqttMessageListener mqttMessageListener;

    public MqttConfig(MqttProperties mqttProperties, MqttMessageListener mqttMessageListener) {
        this.mqttProperties = mqttProperties;
        this.mqttMessageListener = mqttMessageListener;
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setWill("skytrust/server/status", "OFFLINE".getBytes(), mqttProperties.getQos(), true);
        return options;
    }

    @Bean
    public MqttClient mqttClient() {
        try {
            MqttClient client = new MqttClient(
                    mqttProperties.getBrokerUrl(),
                    mqttProperties.getClientId()
            );
            client.setCallback(mqttMessageListener);
            client.connect(mqttConnectOptions());
            client.subscribe("skytrust/device/+/telemetry", mqttProperties.getQos());
            client.subscribe("skytrust/device/+/status", mqttProperties.getQos());
            log.info("MQTT client connected to {}, subscribed to device topics", mqttProperties.getBrokerUrl());
            return client;
        } catch (MqttException e) {
            log.error("Failed to connect MQTT broker at {}: {}", mqttProperties.getBrokerUrl(), e.getMessage());
            log.warn("Application will continue without MQTT - device telemetry will be unavailable");
            return null;
        }
    }
}
