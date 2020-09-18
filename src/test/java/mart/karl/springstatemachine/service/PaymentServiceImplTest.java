package mart.karl.springstatemachine.service;

import mart.karl.springstatemachine.domain.Payment;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.State;

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
    final Optional<Payment> preAuthPayment = paymentRepository.findById(savedPayment.getId());
    assertThat(preAuthPayment)
        .isPresent()
        .get()
        .extracting(Payment::getState)
        .isIn(PaymentState.PRE_AUTH, PaymentState.PRE_AUTH_ERROR);
  }

  @RepeatedTest(10)
  void auth() {
    // Given
    final Payment savedPayment =
        paymentService.createPayment(paymentService.createPayment(payment));
    // When
    StateMachine<PaymentState, PaymentEvent> stateMachine =
        paymentService.preAuth(savedPayment.getId());
    if (stateMachine.getState().getId() == PaymentState.PRE_AUTH) {
      stateMachine = paymentService.authorizePayment(savedPayment.getId());
      // Then
      assertThat(stateMachine)
          .extracting(Region::getState)
          .extracting(State::getId)
          .isIn(PaymentState.AUTH, PaymentState.AUTH_ERROR);
    } else {
      // Then
      assertThat(stateMachine)
          .extracting(Region::getState)
          .extracting(State::getId)
          .isEqualTo(PaymentState.PRE_AUTH_ERROR);
    }
  }
}
