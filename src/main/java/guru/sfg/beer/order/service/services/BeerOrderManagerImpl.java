package guru.sfg.beer.order.service.services;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {
	
	private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
	private final BeerOrderRepository beerOrderRepository;

	@Override
	@Transactional
	public BeerOrder newBeerOrder(BeerOrder beerOrder) {
		beerOrder.setId(null);
		beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
		BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
		sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
		return savedBeerOrder;
	}

	private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
		Message<BeerOrderEventEnum> msg = MessageBuilder.withPayload(event).build();
		stateMachine.sendEvent(msg);
	}
	private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());
		stateMachine.stop();
		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(sma -> {
				sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
			});
		stateMachine.start();
		return stateMachine;
	}
}
