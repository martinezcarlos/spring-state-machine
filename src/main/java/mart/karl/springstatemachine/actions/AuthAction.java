package mart.karl.springstatemachine.actions;

import java.util.Random;
import lombok.extern.log4j.Log4j2;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.service.PaymentServiceImpl;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AuthAction implements Action<PaymentState, PaymentEvent> {
  @Override
  public void execute(final StateContext<PaymentState, PaymentEvent> stateContext) {
    log.debug("Auth was called.");
    final PaymentEvent paymentEvent;
    if (new Random().nextInt(10) < 8) {
      log.debug("Auth approved!!");
      paymentEvent = PaymentEvent.AUTH_APPROVED;
    } else {
      log.debug("Auth declined. No credit!!");
      paymentEvent = PaymentEvent.AUTH_DECLINED;
    }
    stateContext
        .getStateMachine()
        .sendEvent(
            MessageBuilder.withPayload(paymentEvent)
                .setHeader(
                    PaymentServiceImpl.PAYMENT_ID_HEADER,
                    stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                .build());
  }
}
