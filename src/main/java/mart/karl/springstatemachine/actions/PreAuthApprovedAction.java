package mart.karl.springstatemachine.actions;

import lombok.extern.log4j.Log4j2;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PreAuthApprovedAction implements Action<PaymentState, PaymentEvent> {
  @Override
  public void execute(final StateContext<PaymentState, PaymentEvent> stateContext) {
    log.debug("PreAuthApprovedAction was called.");
  }
}
