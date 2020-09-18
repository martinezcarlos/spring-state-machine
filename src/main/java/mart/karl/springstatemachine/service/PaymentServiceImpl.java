package mart.karl.springstatemachine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mart.karl.springstatemachine.domain.Payment;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  public static final String PAYMENT_ID_HEADER = "payment_id";

  private final PaymentRepository paymentRepository;
  private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
  private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

  @Override
  public Payment createPayment(Payment payment) {
    log.debug("Creating new payment: {}", payment);
    payment.setState(PaymentState.NEW);
    return paymentRepository.save(payment);
  }

  @Override
  public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
    log.debug("PreAuthorizing payment with id {}", paymentId);
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
    return sm;
  }

  @Override
  public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
    log.debug("Authorizing payment with id {}", paymentId);
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
    return sm;
  }

  @Override
  public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
    log.debug("Declining payment with id {}", paymentId);
    StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
    sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
    return sm;
  }

  private void sendEvent(
      Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
    final Message<PaymentEvent> msg =
        MessageBuilder.withPayload(event).setHeader(PAYMENT_ID_HEADER, paymentId).build();
    sm.sendEvent(msg);
  }

  private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
    final Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("No payment found for the id %d", paymentId)));
    StateMachine<PaymentState, PaymentEvent> sm =
        stateMachineFactory.getStateMachine(Long.toString(payment.getId()));
    sm.stop();
    sm.getStateMachineAccessor()
        .doWithAllRegions(
            sma -> {
              sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
              sma.resetStateMachine(
                  new DefaultStateMachineContext<>(payment.getState(), null, null, null));
            });
    sm.start();
    return sm;
  }
}
