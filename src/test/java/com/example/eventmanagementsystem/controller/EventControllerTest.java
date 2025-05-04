package com.example.eventmanagementsystem.controller;

import com.example.eventmanagementsystem.dto.EventDTO;
import com.example.eventmanagementsystem.security.EventSecurity;
import com.example.eventmanagementsystem.service.EventService;
import com.example.eventmanagementsystem.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest(EventController.class)
//@Import(TestWebConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private EventSecurity eventSecurity;

    private EventDTO testEventDTO;

    @BeforeEach
    public void setUp() {
        testEventDTO = TestUtils.createTestEventDTO();

        // Mock EventSecurity for authorization checks
        when(eventSecurity.isOrganizerOrAdmin(anyLong(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    public void whenGetAllEvents_thenReturnEventsList() throws Exception {
        // Given
        EventDTO anotherEventDTO = EventDTO.builder()
                .id(2L)
                .name("Another Event")
                .build();

        when(eventService.findAllEvents()).thenReturn(Arrays.asList(testEventDTO, anotherEventDTO));

        // When & Then
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(testEventDTO.getName()))
                .andExpect(jsonPath("$[1].id").value(anotherEventDTO.getId()))
                .andExpect(jsonPath("$[1].name").value(anotherEventDTO.getName()));

        verify(eventService, times(1)).findAllEvents();
    }

    @Test
    @WithMockUser
    public void whenGetAllEvents_withPublishedOnlyParam_thenReturnPublishedEvents() throws Exception {
        // Given
        when(eventService.findAllPublishedEvents()).thenReturn(Collections.singletonList(testEventDTO));

        // When & Then
        mockMvc.perform(get("/api/events")
                        .param("publishedOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(testEventDTO.getName()));

        verify(eventService, times(1)).findAllPublishedEvents();
    }

    @Test
    @WithMockUser
    public void whenGetEventById_thenReturnEvent() throws Exception {
        // Given
        when(eventService.findEventById(anyLong())).thenReturn(testEventDTO);

        // When & Then
        mockMvc.perform(get("/api/events/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$.name").value(testEventDTO.getName()));

        verify(eventService, times(1)).findEventById(1L);
    }

    @Test
    @WithMockUser(roles = {"ORGANIZER"})
    public void whenCreateEvent_withValidData_thenReturnCreatedEvent() throws Exception {
        // Given
        when(eventService.createEvent(any(EventDTO.class))).thenReturn(testEventDTO);

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$.name").value(testEventDTO.getName()));

        verify(eventService, times(1)).createEvent(any(EventDTO.class));
    }

//    @Test
//    @WithMockUser(roles = {"USER"})
//    public void whenCreateEvent_withInsufficientRoles_thenReturn403() throws Exception {
//        // When & Then
//        mockMvc.perform(post("/api/events")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testEventDTO)))
//                .andExpect(status().isForbidden());
//
//        verify(eventService, never()).createEvent(any(EventDTO.class));
//    }

    @Test
    @WithMockUser
    public void whenUpdateEvent_withValidData_thenReturnUpdatedEvent() throws Exception {
        // Given
        when(eventService.updateEvent(anyLong(), any(EventDTO.class))).thenReturn(testEventDTO);

        // When & Then
        mockMvc.perform(put("/api/events/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$.name").value(testEventDTO.getName()));

        verify(eventService, times(1)).updateEvent(eq(1L), any(EventDTO.class));
    }

    @Test
    @WithMockUser
    public void whenDeleteEvent_thenReturn204() throws Exception {
        // Given
        doNothing().when(eventService).deleteEvent(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/events/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent(1L);
    }

    @Test
    @WithMockUser
    public void whenSearchEvents_thenReturnMatchingEvents() throws Exception {
        // Given
        when(eventService.searchEvents(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testEventDTO));

        LocalDate testDate = LocalDate.now().plusDays(7);
        String dateStr = testDate.format(DateTimeFormatter.ISO_DATE);

        // When & Then
        mockMvc.perform(get("/api/events/search")
                        .param("keyword", "test")
                        .param("category", "Test Category")
                        .param("date", dateStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(testEventDTO.getName()));

        verify(eventService, times(1)).searchEvents(eq("test"), eq("Test Category"), eq(testDate));
    }

    @Test
    @WithMockUser
    public void whenGetEventsByOrganizer_thenReturnOrganizerEvents() throws Exception {
        // Given
        when(eventService.findEventsByOrganizer(anyLong()))
                .thenReturn(Collections.singletonList(testEventDTO));

        // When & Then
        mockMvc.perform(get("/api/events/organizer/{organizerId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(testEventDTO.getName()));

        verify(eventService, times(1)).findEventsByOrganizer(3L);
    }

    @Test
    @WithMockUser
    public void whenPublishEvent_thenReturnPublishedEvent() throws Exception {
        // Given
        when(eventService.publishEvent(anyLong())).thenReturn(testEventDTO);

        // When & Then
        mockMvc.perform(patch("/api/events/{id}/publish", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$.name").value(testEventDTO.getName()));

        verify(eventService, times(1)).publishEvent(1L);
    }

    @Test
    @WithMockUser
    public void whenUnpublishEvent_thenReturnUnpublishedEvent() throws Exception {
        // Given
        when(eventService.unpublishEvent(anyLong())).thenReturn(testEventDTO);

        // When & Then
        mockMvc.perform(patch("/api/events/{id}/unpublish", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEventDTO.getId()))
                .andExpect(jsonPath("$.name").value(testEventDTO.getName()));

        verify(eventService, times(1)).unpublishEvent(1L);
    }
}