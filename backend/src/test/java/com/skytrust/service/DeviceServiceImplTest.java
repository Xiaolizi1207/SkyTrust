package com.skytrust.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.skytrust.common.Result;
import com.skytrust.entity.Device;
import com.skytrust.mapper.DeviceMapper;
import com.skytrust.service.impl.DeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Device testDevice;

    @BeforeEach
    void setUp() {
        testDevice = new Device();
        testDevice.setId(1L);
        testDevice.setDeviceName("Test Drone");
        testDevice.setSerialNumber("SN-001");
        testDevice.setStatus(0);
        testDevice.setBatteryLevel(100);
        testDevice.setRentalPrice(new BigDecimal("100.00"));
        testDevice.setInsuranceFee(new BigDecimal("10.00"));
    }

    @Test
    void testUpdateDeviceStatus() {
        when(deviceMapper.selectById(1L)).thenReturn(testDevice);
        when(deviceMapper.updateById(any(Device.class))).thenReturn(1);

        boolean result = deviceService.updateDeviceStatus(1L, 1);
        assertTrue(result);
        assertEquals(1, testDevice.getStatus());
    }

    @Test
    void testUpdateDeviceStatusInvalid() {
        assertThrows(Exception.class, () -> deviceService.updateDeviceStatus(1L, 99));
    }

    @Test
    void testUpdateDeviceLocation() {
        when(deviceMapper.selectById(1L)).thenReturn(testDevice);
        when(deviceMapper.updateById(any(Device.class))).thenReturn(1);

        boolean result = deviceService.updateDeviceLocation(1L,
                new BigDecimal("39.9042"), new BigDecimal("116.4074"), new BigDecimal("100.0"));
        assertTrue(result);
    }

    @Test
    void testIsDeviceAvailable() {
        testDevice.setStatus(1);
        testDevice.setBatteryLevel(80);
        when(deviceMapper.selectById(1L)).thenReturn(testDevice);

        assertTrue(deviceService.isDeviceAvailable(1L));
    }

    @Test
    void testIsDeviceNotAvailableWhenOffline() {
        testDevice.setStatus(0);
        when(deviceMapper.selectById(1L)).thenReturn(testDevice);

        assertFalse(deviceService.isDeviceAvailable(1L));
    }

    @Test
    void testGetDeviceStatistics() {
        when(deviceMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L, 3L, 1L, 1L, 0L, 5L);

        var stats = deviceService.getDeviceStatistics();
        assertNotNull(stats);
        assertEquals(5L, stats.getTotalDevices());
        assertEquals(3L, stats.getOnlineDevices());
        assertEquals(1L, stats.getFlyingDevices());
    }
}
