package com.markokosic.minicrm.modules.car;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser
    void createCar_Success() throws Exception {
        CreateCarRequestDTO requestDTO = new CreateCarRequestDTO("M-AB1234", "Prius", "Toyota", "120");
        CarResponseDTO responseDTO = new CarResponseDTO(1L, "M-AB1234", "Prius", "Toyota", "120", CarStatus.ACTIVE, null, null);

        when(carService.createCar(any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.created")).thenReturn("Car created");

        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.licensePlate").value("M-AB1234"));
    }

    @Test
    @WithMockUser
    void getCar_Success() throws Exception {
        CarResponseDTO responseDTO = new CarResponseDTO(1L, "M-AB1234", "Prius", "Toyota", "120", CarStatus.ACTIVE, null, null);

        when(carService.getCarById(1L)).thenReturn(responseDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Car fetched");

        mockMvc.perform(get("/api/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser
    void getAllCars_Success() throws Exception {
        CarResponseDTO responseDTO = new CarResponseDTO(1L, "M-AB1234", "Prius", "Toyota", "120", CarStatus.ACTIVE, null, null);
        PageResponseDTO<CarResponseDTO> pageResponse = new PageResponseDTO<>(List.of(responseDTO), 1, 10, 1L, 1, true, true);

        when(carService.getAllCars(any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Cars fetched");

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].licensePlate").value("M-AB1234"));
    }

    @Test
    @WithMockUser
    void updateCar_Success() throws Exception {
        UpdateCarRequestDTO requestDTO = new UpdateCarRequestDTO("M-AB9999", "Prius", "Toyota", "120");
        CarResponseDTO responseDTO = new CarResponseDTO(1L, "M-AB9999", "Prius", "Toyota", "120", CarStatus.ACTIVE, null, null);

        when(carService.updateCar(eq(1L), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.updated")).thenReturn("Car updated");

        mockMvc.perform(patch("/api/cars/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.licensePlate").value("M-AB9999"));
    }

    @Test
    @WithMockUser
    void deleteCar_Success() throws Exception {
        doNothing().when(carService).deleteCar(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Car deleted");

        mockMvc.perform(delete("/api/cars/1"))
                .andExpect(status().isNoContent());
    }
}
