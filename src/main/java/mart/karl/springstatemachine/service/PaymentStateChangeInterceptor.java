package mart.karl.springstatemachine.service;

import lombok.RequiredArgsConstructor;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.repository.PaymentRepository;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor
    extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

  private final PaymentRepository paymentRepository;

  @Override
  public void postStateChange(
      State<PaymentState, PaymentEvent> state,
      Message<PaymentEvent> message,
      Transition<PaymentState, PaymentEvent> transition,
      StateMachine<PaymentState, PaymentEvent> stateMachine) {
    Optional.ofNullable(message)
        .map(Message::getHeaders)
        .map(h -> h.getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L))
        .map(Long.class::cast)
        .flatMap(paymentRepository::findById)
        .ifPresent(
            payment -> {
              payment.setState(state.getId());
              paymentRepository.save(payment);
            });
  }
}
