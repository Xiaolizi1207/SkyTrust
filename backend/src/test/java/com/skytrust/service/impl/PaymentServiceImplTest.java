package com.skytrust.service.impl;

import com.skytrust.common.Result;
import com.skytrust.entity.Payment;
import com.skytrust.exception.BusinessException;
import com.skytrust.mapper.PaymentMapper;
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
class PaymentServiceImplTest {

    @Mock private PaymentMapper paymentMapper;
    @InjectMocks private PaymentServiceImpl paymentService;

    @Test void createPayment_shouldSucceedWithValidInput() {
        Payment payment = new Payment();
        payment.setOrderId(100L);
        payment.setAmount(new BigDecimal("299.00"));
        payment.setPaymentMethod("alipay");
        when(paymentMapper.insert(any(Payment.class))).thenReturn(1);

        Result<Payment> result = paymentService.createPayment(payment);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getTransactionNo());
        assertTrue(result.getData().getTransactionNo().startsWith("PAY"));
    }

    @Test void createPayment_shouldRejectMissingOrderId() {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod("wechat");
        assertThrows(BusinessException.class, () -> paymentService.createPayment(payment));
    }

    @Test void createPayment_shouldRejectMissingAmount() {
        Payment payment = new Payment();
        payment.setOrderId(100L);
        payment.setPaymentMethod("wechat");
        assertThrows(BusinessException.class, () -> paymentService.createPayment(payment));
    }

    @Test void createPayment_shouldRejectMissingPaymentMethod() {
        Payment payment = new Payment();
        payment.setOrderId(100L);
        payment.setAmount(new BigDecimal("100.00"));
        assertThrows(BusinessException.class, () -> paymentService.createPayment(payment));
    }

    @Test void updatePaymentStatus_shouldSetPayTimeWhenPaid() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(0);
        when(paymentMapper.selectById(1L)).thenReturn(payment);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);

        boolean result = paymentService.updatePaymentStatus(1L, 1, null);
        assertTrue(result);
        assertNotNull(payment.getPayTime());
    }
}
