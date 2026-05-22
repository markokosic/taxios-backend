package com.markokosic.minicrm.modules.address;

import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.modules.address.dto.request.CreateAddressRequestDTO;
import com.markokosic.minicrm.modules.address.dto.response.AddressResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
@Slf4j
@RequiredArgsConstructor
public class AddressController {

	private final AddressService  addressService;

	@PostMapping
	public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> createAddress(@PathVariable Long customerId, @Valid @RequestBody CreateAddressRequestDTO request) {
		AddressResponseDTO address = addressService.createAddress(customerId, request);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, address, "Successfully added address to customer"));
	}


	@GetMapping
	public ResponseEntity<ApiResponseDTO<List<AddressResponseDTO>>> getAddressesByCustomer(@PathVariable Long customerId) {
		List<AddressResponseDTO> address = addressService.getAddressesByCustomer(customerId);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, address, "Successfully fetched addresses from customer"));
	}


	@GetMapping("/{addressId}")
	public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> getAddressById(@PathVariable Long customerId, @PathVariable Long addressId) {
		AddressResponseDTO address = addressService.getAddressById(customerId, addressId);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, address, "Successfully fetched address"));
	}

	@PutMapping("/{addressId}")
	public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> updateAddress(@PathVariable Long customerId, @PathVariable Long addressId, @Valid @RequestBody CreateAddressRequestDTO request) {
		AddressResponseDTO address = addressService.updateAddress(customerId, addressId, request);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, address, "Successfully updated address"));
	}

	@DeleteMapping("/{addressId}")
	public ResponseEntity<ApiResponseDTO<AddressResponseDTO>> deleteAddress(
			@PathVariable Long customerId,
			@PathVariable Long addressId
	) {
		addressService.deleteAddress(customerId, addressId);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, "Successfully deleted address"));
	};

}
