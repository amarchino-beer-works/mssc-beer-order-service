package guru.sfg.brewery.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto implements Serializable {
	private static final long serialVersionUID = 3545736275716912663L;
	private UUID id;
	private Integer version;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = Shape.STRING)
	private OffsetDateTime createdDate;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = Shape.STRING)
	private OffsetDateTime lastModifiedDate;
	
	private String beerName;
	private String beerStyle;
	private String upc;
	
	@JsonFormat(shape = Shape.STRING)
	private BigDecimal price;
	private Integer quantityOnHand;
}
