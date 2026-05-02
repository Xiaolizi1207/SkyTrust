package com.skytrust.controller;

import com.skytrust.common.ResultCode;
import com.skytrust.iot.MqttPublisher;
import com.skytrust.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IotController.class)
class IotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MqttPublisher mqttPublisher;

    @MockBean
    private DeviceService deviceService;

    @Test
    @WithMockUser
    void testSendHoverCommand() throws Exception {
        doNothing().when(mqttPublisher).publishCommand(1L, "hover");

        mockMvc.perform(post("/api/iot/devices/1/command")
                        .param("command", "hover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser
    void testSendInvalidCommand() throws Exception {
        mockMvc.perform(post("/api/iot/devices/1/command")
                        .param("command", "invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
