package mart.karl.springstatemachine.config;

import java.util.UUID;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

@SpringBootTest
class StateMachineConfigTest {
  @Autowired private StateMachineFactory<PaymentState, PaymentEvent> factory;

  @Test
  void newStateMachine() {
    final StateMachine<PaymentState, PaymentEvent> sm = factory.getStateMachine(UUID.randomUUID());
    sm.start();
    System.out.println(sm.getState());
    sm.sendEvent(PaymentEvent.PRE_AUTHORIZE);
    System.out.println(sm.getState());
    sm.sendEvent(PaymentEvent.PRE_AUTH_APPROVED);
    System.out.println(sm.getState());
  }
}
