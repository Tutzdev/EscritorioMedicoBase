package io.github.officemed.medical_office_api.availability.service;

import io.github.officemed.medical_office_api.availability.dto.CreateProfessionalAvailabilityRequest;
import io.github.officemed.medical_office_api.availability.dto.ProfessionalAvailabilityResponse;
import io.github.officemed.medical_office_api.availability.entity.ProfessionalAvailability;
import io.github.officemed.medical_office_api.availability.exception.AvailabilityConflictException;
import io.github.officemed.medical_office_api.availability.mapper.ProfessionalAvailabilityMapper;
import io.github.officemed.medical_office_api.availability.repository.ProfessionalAvailabilityRepository;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.professional.service.ProfessionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalAvailabilityServiceTest {

    @Mock
    private ProfessionalAvailabilityRepository availabilityRepository;

    @Mock
    private ProfessionalService professionalService;

    @Mock
    private ProfessionalAvailabilityMapper availabilityMapper;

    @Mock
    private Professional professional;

    private ProfessionalAvailabilityService availabilityService;
    private UUID professionalId;
    private CreateProfessionalAvailabilityRequest request;

    @BeforeEach
    void setUp() {
        availabilityService = new ProfessionalAvailabilityService(
                availabilityRepository,
                professionalService,
                availabilityMapper
        );
        professionalId = UUID.randomUUID();
        request = new CreateProfessionalAvailabilityRequest(
                professionalId,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        when(professionalService.findEntityByIdForUpdate(professionalId))
                .thenReturn(professional);
        when(professional.isActive()).thenReturn(true);
        when(professional.getId()).thenReturn(professionalId);
    }

    @Test
    void shouldCreateAvailabilityWhileHoldingProfessionalLock() {
        ProfessionalAvailabilityResponse expectedResponse = mock(
                ProfessionalAvailabilityResponse.class
        );
        when(availabilityRepository.save(any(ProfessionalAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(availabilityMapper.toResponse(any(ProfessionalAvailability.class)))
                .thenReturn(expectedResponse);

        ProfessionalAvailabilityResponse response = availabilityService.create(request);

        assertThat(response).isSameAs(expectedResponse);
        verify(professionalService).findEntityByIdForUpdate(professionalId);
        verify(availabilityRepository).save(any(ProfessionalAvailability.class));
    }

    @Test
    void shouldRejectOverlappingAvailability() {
        when(availabilityRepository.existsOverlapping(
                professionalId,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        )).thenReturn(true);

        assertThatThrownBy(() -> availabilityService.create(request))
                .isInstanceOf(AvailabilityConflictException.class);

        verify(availabilityRepository, never()).save(any());
    }
}
