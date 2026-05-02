package com.skytrust.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "skytrust.iot.mqtt")
public class MqttProperties {

    private String brokerUrl = "tcp://localhost:1883";
    private String username = "skytrust";
    private String password = "skytrust123";
    private String clientId = "skytrust-server";
    private int qos = 1;
    private int reconnectInterval = 5000;
}
