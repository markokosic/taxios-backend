package com.markokosic.minicrm.modules.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.markokosic.minicrm.modules.customer.model.CustomerType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "type",
		visible = true
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = CreateConsumerCustomerRequestDTO.class, name = "CONSUMER"),
		@JsonSubTypes.Type(value = CreateBusinessCustomerRequestDTO.class, name = "BUSINESS")
})
public class CreateCustomerRequestDTO {

	@NotNull
	private CustomerType type;

}
