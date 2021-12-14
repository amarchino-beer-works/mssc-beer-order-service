package guru.sfg.beer.order.service.services.beer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import guru.sfg.brewery.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Component
@RequiredArgsConstructor
public class BeerServiceRestTemplateImpl implements BeerService {
	private static final String BEER_PATH_V1 = "/api/v1/beer/{id}";
	private static final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/{upc}";
	private final RestTemplate restTemplate;
	
	@Setter
	private String beerServiceHost;

	@Override
	public Optional<BeerDto> getBeerById(UUID id) {
		return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH_V1, BeerDto.class, id));
	}

	@Override
	public Optional<BeerDto> getBeerByUpc(String upc) {
		return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH_V1, BeerDto.class, upc));
	}

}
