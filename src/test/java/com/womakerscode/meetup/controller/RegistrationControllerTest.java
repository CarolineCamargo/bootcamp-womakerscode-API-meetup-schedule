package com.womakerscode.meetup.controller;

import com.womakerscode.meetup.controller.exceptions.BusinessException;
import com.womakerscode.meetup.model.dto.RegistrationDTO;
import com.womakerscode.meetup.model.entity.Registration;
import com.womakerscode.meetup.service.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles ("test")
@WebMvcTest (controllers = {RegistrationController.class})
@AutoConfigureMockMvc
public class RegistrationControllerTest {

    static String REGISTRATION_API = "/api/registration";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegistrationService registrationService;

    @Test
    @DisplayName("Should create a registration with success")
    public void createRegistration() throws Exception{

        RegistrationDTO dto = createNewRegistrationDTO();
        Registration registration = createNewRegistration();

        BDDMockito.given(registrationService.save(any(Registration.class))).willReturn(registration);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(dto.getId()))
                .andExpect(jsonPath("name").value(dto.getName()))
                .andExpect(jsonPath("dateOfRegistration").value(dto.getDateOfRegistration()))
                .andExpect(jsonPath("cpf").value(dto.getCpf()));

    }

    @Test
    @DisplayName("Should throw an exception when not have data enough for the test")
    public void createInvalidRegistration() throws Exception{

        String json = new ObjectMapper().writeValueAsString(new RegistrationDTO());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw an exception when try to created a new registration that already created")
    public void createDuplicateRegistration() throws Exception{

        String json = new ObjectMapper().writeValueAsString(createNewRegistrationDTO());

        BDDMockito.given(registrationService.save(any(Registration.class)))
                .willThrow(new BusinessException("Registration already created!"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(REGISTRATION_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Registration already created!"));
    }

    @Test
    @DisplayName("Should get registration")
    public void getRegistration() throws Exception {

        RegistrationDTO registrationDTO = createNewRegistrationDTO();
        Registration registration = createNewRegistration();

        BDDMockito.given(registrationService.getRegistrationById(registration.getId())).willReturn(Optional.of(registration));
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REGISTRATION_API.concat("/" + registration.getId()))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(registration.getId()))
                .andExpect(jsonPath("name").value(registrationDTO.getName()))
                .andExpect(jsonPath("dateOfRegistration").value(registrationDTO.getDateOfRegistration()))
                .andExpect(jsonPath("cpf").value(registrationDTO.getCpf()));
    }

    @Test
    @DisplayName("Should return not found when the registration doesn't exists")
    public void registrationNotFound() throws Exception{

        BDDMockito.given(registrationService.getRegistrationById(anyInt())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REGISTRATION_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete the registration")
    public void deleteRegistration() throws Exception{

        BDDMockito.given(registrationService.getRegistrationById(anyInt()))
                .willReturn(Optional.of(createNewRegistration()));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete(REGISTRATION_API.concat("/" + 101))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return resource not found when no registration is found to delete")
    public void deleteNonExistentRegistration() throws Exception{

        BDDMockito.given(registrationService.getRegistrationById(anyInt())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete(REGISTRATION_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update a registration existing")
    public void updateRegistration() throws Exception {

        RegistrationDTO dto = createNewRegistrationDTO();
        Registration updatingRegistration = updatingRegistration();
        Registration updatedRegistration = createNewRegistration();

        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(registrationService.getRegistrationById(anyInt())).willReturn(Optional.of(updatingRegistration));
        BDDMockito.given(registrationService.update(updatingRegistration)).willReturn(updatedRegistration);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .put(REGISTRATION_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(dto.getId()))
                .andExpect(jsonPath("name").value(dto.getName()))
                .andExpect(jsonPath("dateOfRegistration").value(dto.getDateOfRegistration()))
                .andExpect(jsonPath("cpf").value(dto.getCpf()));
    }


    @Test
    @DisplayName("Should return resource not found when no registration is found to update")
    public void updateNonExistentRegistration() throws Exception {

        String json = new ObjectMapper().writeValueAsString(createNewRegistrationDTO());

        BDDMockito.given(registrationService.getRegistrationById(anyInt())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .put(REGISTRATION_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter registration")
    public void findRegistration() throws Exception {

        Registration registration = createNewRegistration();

        BDDMockito.given(registrationService.find(any(Registration.class), any(Pageable.class)))
                .willReturn(new PageImpl<Registration>(List.of(registration), PageRequest.of(0, 100), 1));

        String queryString = String.format("?name=%s&dateOfRegistration=%s&page=0&size=100",
                registration.getName(), registration.getDateOfRegistration());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(REGISTRATION_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private RegistrationDTO createNewRegistrationDTO() {
        return RegistrationDTO.builder()
                .id(101)
                .name("Caroline")
                .dateOfRegistration("01/04/2022")
                .cpf("012345678900")
                .build();
    }

    private Registration createNewRegistration() {
        return Registration.builder()
                .id(101)
                .name("Caroline")
                .dateOfRegistration("01/04/2022")
                .cpf("012345678900")
                .build();
    }

    private Registration updatingRegistration() {
        return Registration.builder()
                .id(101)
                .name("Regina")
                .dateOfRegistration("20/04/2022")
                .cpf("012345678900")
                .build();
    }
}
