package guru.sfg.beer.order.service.services.listeners;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import guru.sfg.brewery.util.JmsQueues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationResultListener {

	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsQueues.ALLOCATE_ORDER_RESPONSE_QUEUE)
	public void listen(AllocateOrderResult result) {
		log.debug("Validation result for order id: " + result.getBeerOrderDto().getId());
		if(!result.getAllocationError() && !result.getPendingInventory()) {
			// Allocated normally
			beerOrderManager.processAllocationPassed(result.getBeerOrderDto());
		} else if(!result.getAllocationError() && result.getPendingInventory()) {
			// Allocated normally
			beerOrderManager.processAllocationPendingInventory(result.getBeerOrderDto());
		} else if(result.getAllocationError()) {
			// Allocated normally
			beerOrderManager.processAllocationFailed(result.getBeerOrderDto());
		}
	}
}
