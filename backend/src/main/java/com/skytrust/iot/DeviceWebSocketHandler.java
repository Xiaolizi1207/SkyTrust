package com.skytrust.iot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket connected: {}, total: {}", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client can send filter requests, e.g. {"action":"subscribe","deviceId":123}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket disconnected: {}, total: {}", session.getId(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage());
        sessions.remove(session.getId());
    }

    public void broadcastDeviceUpdate(Long deviceId, JSONObject telemetry) {
        JSONObject msg = new JSONObject();
        msg.put("type", "device_update");
        msg.put("deviceId", deviceId);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("data", telemetry);

        String payload = msg.toJSONString();
        TextMessage textMessage = new TextMessage(payload);

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.debug("Failed to send to WebSocket {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    public int getConnectedCount() {
        return sessions.size();
    }
}
