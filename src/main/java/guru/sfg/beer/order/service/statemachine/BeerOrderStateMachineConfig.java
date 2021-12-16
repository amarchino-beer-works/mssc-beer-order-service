package guru.sfg.beer.order.service.statemachine;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.statemachine.actions.AllocateOrderAction;
import guru.sfg.beer.order.service.statemachine.actions.ValidateOrderAction;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class BeerOrderStateMachineConfig extends StateMachineConfigurerAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {
	
	private final ValidateOrderAction validateOrderAction;
	private final AllocateOrderAction allocateOrderAction;

	@Override
	public void configure(StateMachineStateConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> states) throws Exception {
		states.withStates()
			.initial(BeerOrderStatusEnum.NEW)
			.states(EnumSet.allOf(BeerOrderStatusEnum.class))
			.end(BeerOrderStatusEnum.DELIVERED)
			.end(BeerOrderStatusEnum.PICKED_UP)
			.end(BeerOrderStatusEnum.DELIVERY_EXCEPTION)
			.end(BeerOrderStatusEnum.VALIDATION_EXCEPTION)
			.end(BeerOrderStatusEnum.ALLOCATION_EXCEPTION);
	}
	
	@Override
	public void configure(StateMachineTransitionConfigurer<BeerOrderStatusEnum, BeerOrderEventEnum> transitions) throws Exception {
		transitions
			.withExternal()
				.state(BeerOrderStatusEnum.NEW).target(BeerOrderStatusEnum.VALIDATION_PENDING).event(BeerOrderEventEnum.VALIDATE_ORDER).action(validateOrderAction)
			.and().withExternal()
				.state(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATED).event(BeerOrderEventEnum.VALIDATION_PASSED)
			.and().withExternal()
				.state(BeerOrderStatusEnum.VALIDATION_PENDING).target(BeerOrderStatusEnum.VALIDATION_EXCEPTION).event(BeerOrderEventEnum.VALIDATION_FAILED)
			.and().withExternal()
				.state(BeerOrderStatusEnum.VALIDATED).target(BeerOrderStatusEnum.ALLOCATION_PENDING).event(BeerOrderEventEnum.ALLOCATE_ORDER).action(allocateOrderAction)
			.and().withExternal()
				.state(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATED).event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
			.and().withExternal()
				.state(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)
			.and().withExternal()
				.state(BeerOrderStatusEnum.ALLOCATION_PENDING).target(BeerOrderStatusEnum.PENDING_INVENTORY).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
	}
	
}
