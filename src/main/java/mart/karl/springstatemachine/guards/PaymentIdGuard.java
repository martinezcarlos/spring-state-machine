package mart.karl.springstatemachine.guards;

import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import mart.karl.springstatemachine.service.PaymentServiceImpl;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

@Component
public class PaymentIdGuard implements Guard<PaymentState, PaymentEvent> {
  @Override
  public boolean evaluate(final StateContext<PaymentState, PaymentEvent> stateContext) {
    return stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
  }
}
