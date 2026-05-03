package com.skytrust.service.impl;

import com.skytrust.common.Result;
import com.skytrust.entity.RentalOrder;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.RentalOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RentalOrderServiceImplTest {

    @Mock private RentalOrderMapper rentalOrderMapper;
    @InjectMocks private RentalOrderServiceImpl orderService;

    @Test void createOrder_shouldSucceedWithValidInput() {
        RentalOrder order = new RentalOrder();
        order.setUserId(1L);
        order.setDeviceId(10L);
        order.setStartTime(LocalDateTime.now());
        order.setEndTime(LocalDateTime.now().plusHours(2));
        order.setTotalAmount(new BigDecimal("199.00"));
        when(rentalOrderMapper.insert(any(RentalOrder.class))).thenReturn(1);

        Result<RentalOrder> result = orderService.createOrder(order);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getOrderNo());
    }

    @Test void createOrder_shouldRejectMissingUserId() {
        RentalOrder order = new RentalOrder();
        order.setDeviceId(10L);
        order.setTotalAmount(new BigDecimal("100.00"));
        assertThrows(BusinessException.class, () -> orderService.createOrder(order));
    }

    @Test void createOrder_shouldRejectMissingDeviceId() {
        RentalOrder order = new RentalOrder();
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("100.00"));
        assertThrows(BusinessException.class, () -> orderService.createOrder(order));
    }

    @Test void createOrder_shouldAutoGenerateOrderNoWhenEmpty() {
        RentalOrder order = new RentalOrder();
        order.setUserId(1L);
        order.setDeviceId(10L);
        order.setStartTime(LocalDateTime.now());
        order.setEndTime(LocalDateTime.now().plusHours(1));
        order.setTotalAmount(new BigDecimal("50.00"));
        when(rentalOrderMapper.insert(any(RentalOrder.class))).thenReturn(1);

        Result<RentalOrder> result = orderService.createOrder(order);
        assertTrue(result.getData().getOrderNo().startsWith("RO"));
    }

    @Test void createOrder_shouldDefaultStatusToPendingPayment() {
        RentalOrder order = new RentalOrder();
        order.setUserId(1L);
        order.setDeviceId(10L);
        order.setStartTime(LocalDateTime.now());
        order.setEndTime(LocalDateTime.now().plusHours(1));
        order.setTotalAmount(new BigDecimal("80.00"));
        when(rentalOrderMapper.insert(any(RentalOrder.class))).thenReturn(1);

        Result<RentalOrder> result = orderService.createOrder(order);
        assertEquals(0, result.getData().getStatus());
    }
}
