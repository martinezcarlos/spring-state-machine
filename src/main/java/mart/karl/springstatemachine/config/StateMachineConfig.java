package mart.karl.springstatemachine.config;

import java.util.EnumSet;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mart.karl.springstatemachine.domain.PaymentEvent;
import mart.karl.springstatemachine.domain.PaymentState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

  private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
  private final Action<PaymentState, PaymentEvent> preAuthAction;
  private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
  private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
  private final Action<PaymentState, PaymentEvent> authAction;
  private final Action<PaymentState, PaymentEvent> authApprovedAction;
  private final Action<PaymentState, PaymentEvent> authDeclinedAction;

  @Override
  public void configure(
      final StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config)
      throws Exception {
    final StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter =
        new StateMachineListenerAdapter<>() {
          @Override
          public void stateChanged(
              final State<PaymentState, PaymentEvent> from,
              final State<PaymentState, PaymentEvent> to) {
            log.info(
                "State changed from {} to {}",
                Optional.ofNullable(from).map(State::getId).orElse(null),
                Optional.ofNullable(to).map(State::getId).orElse(null));
          }
        };
    config.withConfiguration().listener(adapter);
  }

  @Override
  public void configure(final StateMachineStateConfigurer<PaymentState, PaymentEvent> states)
      throws Exception {
    states
        .withStates()
        .initial(PaymentState.NEW)
        .states(EnumSet.allOf(PaymentState.class))
        .end(PaymentState.AUTH)
        .end(PaymentState.PRE_AUTH_ERROR)
        .end(PaymentState.AUTH_ERROR);
  }

  @Override
  public void configure(
      final StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions)
      throws Exception {
    transitions
        .withExternal()
        .source(PaymentState.NEW)
        .target(PaymentState.NEW)
        .event(PaymentEvent.PRE_AUTHORIZE)
        .action(preAuthAction)
        .guard(paymentIdGuard)
        .and()
        .withExternal()
        .source(PaymentState.NEW)
        .target(PaymentState.PRE_AUTH)
        .event(PaymentEvent.PRE_AUTH_APPROVED)
        .action(preAuthApprovedAction)
        .and()
        .withExternal()
        .source(PaymentState.NEW)
        .target(PaymentState.PRE_AUTH_ERROR)
        .event(PaymentEvent.PRE_AUTH_DECLINED)
        .action(preAuthDeclinedAction)
        // PRE_AUTH to AUTH
        .and()
        .withExternal()
        .source(PaymentState.PRE_AUTH)
        .target(PaymentState.PRE_AUTH)
        .event(PaymentEvent.AUTHORIZE)
        .action(authAction)
        .and()
        .withExternal()
        .source(PaymentState.PRE_AUTH)
        .target(PaymentState.AUTH)
        .event(PaymentEvent.AUTH_APPROVED)
        .action(authApprovedAction)
        .and()
        .withExternal()
        .source(PaymentState.PRE_AUTH)
        .target(PaymentState.AUTH_ERROR)
        .event(PaymentEvent.AUTH_DECLINED)
        .action(authDeclinedAction);
  }
}
