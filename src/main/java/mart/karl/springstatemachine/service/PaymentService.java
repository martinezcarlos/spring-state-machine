package mart.karl.springstatemachine.service;

import mart.karl.springstatemachine.domain.Payment;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

  Payment createPayment(Payment payment);

  StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

  StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);
}
