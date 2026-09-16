package io.github.officemed.medical_office_api.availability.service;

import io.github.officemed.medical_office_api.availability.dto.CreateProfessionalAvailabilityRequest;
import io.github.officemed.medical_office_api.availability.dto.ProfessionalAvailabilityResponse;
import io.github.officemed.medical_office_api.availability.dto.UpdateAvailabilityStatusRequest;
import io.github.officemed.medical_office_api.availability.dto.UpdateProfessionalAvailabilityRequest;
import io.github.officemed.medical_office_api.availability.entity.ProfessionalAvailability;
import io.github.officemed.medical_office_api.availability.exception.AvailabilityConflictException;
import io.github.officemed.medical_office_api.availability.exception.ProfessionalAvailabilityNotFoundException;
import io.github.officemed.medical_office_api.availability.mapper.ProfessionalAvailabilityMapper;
import io.github.officemed.medical_office_api.availability.repository.ProfessionalAvailabilityRepository;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.professional.service.ProfessionalService;
import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class ProfessionalAvailabilityService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProfessionalAvailabilityRepository availabilityRepository;
    private final ProfessionalService professionalService;
    private final ProfessionalAvailabilityMapper availabilityMapper;

    public ProfessionalAvailabilityService(
            ProfessionalAvailabilityRepository availabilityRepository,
            ProfessionalService professionalService,
            ProfessionalAvailabilityMapper availabilityMapper
    ) {
        this.availabilityRepository = availabilityRepository;
        this.professionalService = professionalService;
        this.availabilityMapper = availabilityMapper;
    }

    @Transactional
    public ProfessionalAvailabilityResponse create(
            CreateProfessionalAvailabilityRequest request
    ) {
        Professional professional = professionalService.findEntityByIdForUpdate(
                request.professionalId()
        );

        if (!professional.isActive()) {
            throw new BusinessRuleException("Professional must be active");
        }

        ensureValidInterval(request.startTime(), request.endTime());
        ensureNoConflict(
                professional.getId(),
                null,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        ProfessionalAvailability availability = ProfessionalAvailability.create(
                professional,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return availabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProfessionalAvailabilityResponse> findByProfessional(
            UUID professionalId,
            Pageable pageable
    ) {
        professionalService.findEntityById(professionalId);

        Page<ProfessionalAvailabilityResponse> availability = availabilityRepository
                .findByProfessionalId(professionalId, limit(pageable))
                .map(availabilityMapper::toResponse);

        return PageResponse.from(availability);
    }

    @Transactional
    public ProfessionalAvailabilityResponse update(
            UUID availabilityId,
            UpdateProfessionalAvailabilityRequest request
    ) {
        ProfessionalAvailability availability = findEntityById(availabilityId);
        professionalService.findEntityByIdForUpdate(
                availability.getProfessional().getId()
        );

        ensureValidInterval(request.startTime(), request.endTime());
        ensureNoConflict(
                availability.getProfessional().getId(),
                availabilityId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        availability.update(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return availabilityMapper.toResponse(availability);
    }

    @Transactional
    public ProfessionalAvailabilityResponse updateStatus(
            UUID availabilityId,
            UpdateAvailabilityStatusRequest request
    ) {
        ProfessionalAvailability availability = findEntityById(availabilityId);

        if (request.active()) {
            professionalService.findEntityByIdForUpdate(
                    availability.getProfessional().getId()
            );
            ensureNoConflict(
                    availability.getProfessional().getId(),
                    availabilityId,
                    availability.getDayOfWeek(),
                    availability.getStartTime(),
                    availability.getEndTime()
            );
            availability.activate();
        } else {
            availability.deactivate();
        }

        return availabilityMapper.toResponse(availability);
    }

    @Transactional(readOnly = true)
    public boolean covers(
            UUID professionalId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return availabilityRepository.existsCovering(
                professionalId,
                dayOfWeek,
                startTime,
                endTime
        );
    }

    private ProfessionalAvailability findEntityById(UUID availabilityId) {
        return availabilityRepository
                .findById(availabilityId)
                .orElseThrow(() -> new ProfessionalAvailabilityNotFoundException(
                        availabilityId
                ));
    }

    private void ensureValidInterval(
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private void ensureNoConflict(
            UUID professionalId,
            UUID availabilityId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {
        boolean hasConflict = availabilityId == null
                ? availabilityRepository.existsOverlapping(
                        professionalId,
                        dayOfWeek,
                        startTime,
                        endTime
                )
                : availabilityRepository.existsOverlappingExcluding(
                        professionalId,
                        availabilityId,
                        dayOfWeek,
                        startTime,
                        endTime
                );

        if (hasConflict) {
            throw new AvailabilityConflictException();
        }
    }

    private Pageable limit(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }
}
