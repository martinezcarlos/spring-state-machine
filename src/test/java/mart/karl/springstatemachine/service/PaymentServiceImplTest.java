package mart.karl.springstatemachine.service;

import mart.karl.springstatemachine.domain.Payment;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceImplTest {

  @Autowired private PaymentService paymentService;
  @Autowired private PaymentRepository paymentRepository;

  private Payment payment;

  @BeforeEach
  void setUp() {
    payment = Payment.builder().amount(new BigDecimal("12.99")).build();
  }

  @Test
  void preAuth() {
    // Given
    final Payment savedPayment =
        paymentService.createPayment(paymentService.createPayment(payment));
    // When
    paymentService.preAuth(savedPayment.getId());
    // Then
    final Optional<Payment> authorizedPayment = paymentRepository.findById(savedPayment.getId());
    assertThat(authorizedPayment)
        .isPresent()
        .get()
        .extracting(Payment::getState)
        .isIn(PaymentState.PRE_AUTH, PaymentState.PRE_AUTH_ERROR);
  }
}
