package guru.sfg.beer.order.service.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.beer.BeerServiceRestTemplateImpl;
import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerPagedList;

@SpringBootTest
@ExtendWith(WireMockExtension.class)
public class BeerOrderManagerImplIT {

	@Autowired BeerOrderManager beerOrderManager;
	@Autowired BeerOrderRepository beerOrderRepository;
	@Autowired CustomerRepository customerRepository;
	@Autowired WireMockServer wireMockServer;
	@Autowired ObjectMapper objectMapper;
	
	Customer testCustomer;
	UUID beerId = UUID.randomUUID();
	
	@TestConfiguration
	static class RestTemplateBuilderProvider {
		@Bean(destroyMethod = "stop")
		public WireMockServer wiremockServer() {
			WireMockServer server = ManagedWireMockServer.with(WireMockConfiguration.wireMockConfig().port(8083));
			server.start();
			return server;
		}
	}
	
	@BeforeEach
	void setUp() {
		testCustomer = customerRepository.save(Customer.builder().customerName("Test customer").build());
	}
	
	@Test
	void testNewToAllocate() throws JsonProcessingException {
		BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();
		
		wireMockServer.stubFor(WireMock.get(BeerServiceRestTemplateImpl.BEER_UPC_PATH_V1 + "12345").willReturn(WireMock.okJson(objectMapper.writeValueAsString(beerDto))));
		BeerOrder beerOrder = createBeerOrder();
		BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
		Assertions.assertNotNull(savedBeerOrder);
		Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder.getOrderStatus());
	}
	
	BeerOrder createBeerOrder() {
		BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();
		Set<BeerOrderLine> lines = new HashSet<>();
		lines.add(BeerOrderLine.builder().beerId(beerId).orderQuantity(1).upc("12345").beerOrder(beerOrder).build());
		beerOrder.setBeerOrderLines(lines);
		return beerOrder;
	}
}




