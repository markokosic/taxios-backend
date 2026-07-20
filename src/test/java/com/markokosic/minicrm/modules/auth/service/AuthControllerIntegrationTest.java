package com.markokosic.minicrm.modules.auth.service;


import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "/application-test.properties")
public class AuthControllerIntegrationTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	public static class RegisterTenantApiResponse extends ApiResponseDTO<RegisterTenantResponseDTO> {}

	@Test
	void testRegister_whenValidDetailsProvided_shouldReturnTenantDetails() throws JSONException {

		String unique = String.valueOf(System.currentTimeMillis() % 100000000L);
		JSONObject registerTenantDetailsRequestJson = new JSONObject();
		registerTenantDetailsRequestJson.put("tenantName", "tenant" + unique);
		registerTenantDetailsRequestJson.put("password", "testPassword");
		registerTenantDetailsRequestJson.put("firstName", "Max");
		registerTenantDetailsRequestJson.put("lastName", "Mustermann");
		registerTenantDetailsRequestJson.put("email", "test" + unique + "@email.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		HttpEntity<String> request = new HttpEntity<>(registerTenantDetailsRequestJson.toString(), headers);

		//ACT
		ResponseEntity<RegisterTenantApiResponse> createdTenantDetailsEntity =  this.testRestTemplate.postForEntity("/api/auth/register",
				request,
				RegisterTenantApiResponse.class);

		RegisterTenantApiResponse response = createdTenantDetailsEntity.getBody();

		//Assert
		Assertions.assertEquals(HttpStatus.OK, createdTenantDetailsEntity.getStatusCode());
		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.isSuccess());
		
		RegisterTenantResponseDTO createdTenantDetails = response.getData();
		Assertions.assertNotNull(createdTenantDetails);
		Assertions.assertEquals(registerTenantDetailsRequestJson.getString("tenantName"), createdTenantDetails.getTenantName(), "Returned tenants name seems to be incorrect");



	}


}
