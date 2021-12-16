package guru.sfg.beer.order.service.services;

import java.util.UUID;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.brewery.model.BeerOrderDto;

public interface BeerOrderManager {

	BeerOrder newBeerOrder(BeerOrder beerOrder);

	void processValidationResult(UUID orderId, Boolean valid);
	
	void processAllocationPassed(BeerOrderDto beerOrderDto);
	void processAllocationPendingInventory(BeerOrderDto beerOrderDto);
	void processAllocationFailed(BeerOrderDto beerOrderDto);
}
