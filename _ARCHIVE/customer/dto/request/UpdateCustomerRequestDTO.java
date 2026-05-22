package com.markokosic.minicrm.modules.customer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@JsonTypeInfo(
//		use = JsonTypeInfo.Id.NAME,
//		include = JsonTypeInfo.As.PROPERTY,
//		property = "type",
//		visible = true
//)
//@JsonSubTypes({
//		@JsonSubTypes.Type(value = UpdateConsumerCustomerRequestDTO.class, name = "CONSUMER"),
//		@JsonSubTypes.Type(value = UpdateBusinessCustomerRequestDTO.class, name = "BUSINESS")
//})
public class UpdateCustomerRequestDTO {
	private Long id;
}
