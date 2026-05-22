package com.markokosic.minicrm.modules.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.markokosic.minicrm.modules.customer.model.CustomerType;
import lombok.Data;

@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = ConsumerCustomerResponseDTO.class, name = "CONSUMER"),
		@JsonSubTypes.Type(value = BusinessCustomerResponseDTO.class, name = "BUSINESS")
})
public class CustomerResponseDTO {
private Long id;
private CustomerType type;
private String displayName;
//private CustomerStatus status;
private Long tenantId;
}
