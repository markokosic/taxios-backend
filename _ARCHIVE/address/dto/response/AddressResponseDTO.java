package com.markokosic.minicrm.modules.address.dto.response;

import com.markokosic.minicrm.modules.address.model.AddressType;
import lombok.Data;

@Data
public class AddressResponseDTO {
	private Long id;
	private String street;
	private String city;
	private String postalCode;
	private String country;
	private AddressType type;
}
