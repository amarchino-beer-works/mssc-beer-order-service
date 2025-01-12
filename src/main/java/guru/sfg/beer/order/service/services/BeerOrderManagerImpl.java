package guru.sfg.beer.order.service.services;

import java.util.UUID;

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
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {
	
	public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";
	private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
	private final BeerOrderRepository beerOrderRepository;
	private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

	@Override
	@Transactional
	public BeerOrder newBeerOrder(BeerOrder beerOrder) {
		beerOrder.setId(null);
		beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
		BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
		sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
		return savedBeerOrder;
	}
	
	@Override
	public void processValidationResult(UUID orderId, Boolean valid) {
		BeerOrder beerOrder = beerOrderRepository.getById(orderId);
		if(Boolean.TRUE.equals(valid)) {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
			BeerOrder validatedOrder = beerOrderRepository.findOneById(orderId);
			sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
		} else {
			sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
		}
	}
	
	@Override
	public void processAllocationPassed(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getById(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
		updateAllocatedQuantity(beerOrderDto, beerOrder);
	}

	@Override
	public void processAllocationPendingInventory(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getById(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
		updateAllocatedQuantity(beerOrderDto, beerOrder);
	}

	@Override
	public void processAllocationFailed(BeerOrderDto beerOrderDto) {
		BeerOrder beerOrder = beerOrderRepository.getById(beerOrderDto.getId());
		sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
	}

	private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum event) {
		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);
		Message<BeerOrderEventEnum> msg = MessageBuilder
				.withPayload(event)
				.setHeader(ORDER_ID_HEADER, beerOrder.getId())
				.build();
		stateMachine.sendEvent(msg);
	}
	private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
		StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());
		stateMachine.stop();
		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(sma -> {
				sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
				sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
			});
		stateMachine.start();
		return stateMachine;
	}
	
	private void updateAllocatedQuantity(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
		BeerOrder allocatedOrder = beerOrderRepository.getById(beerOrderDto.getId());
		allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
			beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
				if(beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
					beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
				}
			});
		});
		beerOrderRepository.save(beerOrder);
	}

}
