package guru.sfg.beer.order.service.services.listeners;

import java.util.UUID;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import guru.sfg.brewery.util.JmsQueues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationResultListener {
	
	private final BeerOrderManager beerOrderManager;

	@JmsListener(destination = JmsQueues.VALIDATE_ORDER_RESPONSE_QUEUE)
	public void listen(ValidateOrderResult result) {
		UUID orderId = result.getOrderId();
		log.debug("Validation result for order id: " + orderId);
		beerOrderManager.processValidationResult(orderId, result.getValid());
	}
}
