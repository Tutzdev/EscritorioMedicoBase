package io.github.officemed.medical_office_api.professional.service;

import io.github.officemed.medical_office_api.professional.dto.CreateProfessionalRequest;
import io.github.officemed.medical_office_api.professional.dto.ProfessionalResponse;
import io.github.officemed.medical_office_api.professional.dto.UpdateProfessionalRequest;
import io.github.officemed.medical_office_api.professional.dto.UpdateProfessionalStatusRequest;
import io.github.officemed.medical_office_api.professional.entity.Professional;
import io.github.officemed.medical_office_api.professional.exception.ProfessionalNotFoundException;
import io.github.officemed.medical_office_api.professional.exception.RegistrationAlreadyInUseException;
import io.github.officemed.medical_office_api.professional.exception.UserAlreadyLinkedToProfessionalException;
import io.github.officemed.medical_office_api.professional.mapper.ProfessionalMapper;
import io.github.officemed.medical_office_api.professional.repository.ProfessionalRepository;
import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import io.github.officemed.medical_office_api.specialty.service.SpecialtyService;
import io.github.officemed.medical_office_api.user.entity.Role;
import io.github.officemed.medical_office_api.user.entity.User;
import io.github.officemed.medical_office_api.user.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProfessionalService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProfessionalRepository professionalRepository;
    private final SpecialtyService specialtyService;
    private final UserService userService;
    private final ProfessionalMapper professionalMapper;

    public ProfessionalService(
            ProfessionalRepository professionalRepository,
            SpecialtyService specialtyService,
            UserService userService,
            ProfessionalMapper professionalMapper
    ) {
        this.professionalRepository = professionalRepository;
        this.specialtyService = specialtyService;
        this.userService = userService;
        this.professionalMapper = professionalMapper;
    }

    @Transactional
    public ProfessionalResponse create(CreateProfessionalRequest request) {
        Specialty specialty = specialtyService.findEntityById(request.specialtyId());
        ensureSpecialtyIsActive(specialty);
        ensureRegistrationIsAvailable(
                request.registrationNumber(),
                request.registrationState(),
                null
        );

        User user = findAndValidateUser(request.userId());

        Professional professional = Professional.create(
                user,
                specialty,
                request.name(),
                request.registrationNumber(),
                request.registrationState(),
                request.phone(),
                request.email()
        );

        return professionalMapper.toResponse(professionalRepository.save(professional));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProfessionalResponse> findAll(
            String search,
            UUID specialtyId,
            Boolean active,
            Pageable pageable
    ) {
        Page<ProfessionalResponse> professionals = professionalRepository
                .findAll(
                        buildSpecification(search, specialtyId, active),
                        limit(pageable)
                )
                .map(professionalMapper::toResponse);

        return PageResponse.from(professionals);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse findById(UUID professionalId) {
        return professionalMapper.toResponse(findEntityById(professionalId));
    }

    @Transactional
    public ProfessionalResponse update(
            UUID professionalId,
            UpdateProfessionalRequest request
    ) {
        Professional professional = findEntityById(professionalId);
        Specialty specialty = specialtyService.findEntityById(request.specialtyId());

        ensureSpecialtyIsActive(specialty);
        ensureRegistrationIsAvailable(
                request.registrationNumber(),
                request.registrationState(),
                professionalId
        );

        professional.update(
                specialty,
                request.name(),
                request.registrationNumber(),
                request.registrationState(),
                request.phone(),
                request.email()
        );

        return professionalMapper.toResponse(professional);
    }

    @Transactional
    public ProfessionalResponse updateStatus(
            UUID professionalId,
            UpdateProfessionalStatusRequest request
    ) {
        Professional professional = findEntityById(professionalId);

        if (request.active()) {
            professional.activate();
        } else {
            professional.deactivate();
        }

        return professionalMapper.toResponse(professional);
    }

    @Transactional(readOnly = true)
    public Professional findEntityById(UUID professionalId) {
        return professionalRepository
                .findById(professionalId)
                .orElseThrow(() -> new ProfessionalNotFoundException(professionalId));
    }

    @Transactional
    public Professional findEntityByIdForUpdate(UUID professionalId) {
        return professionalRepository
                .findByIdForUpdate(professionalId)
                .orElseThrow(() -> new ProfessionalNotFoundException(professionalId));
    }

    private User findAndValidateUser(UUID userId) {
        if (userId == null) {
            return null;
        }

        if (professionalRepository.existsByUserId(userId)) {
            throw new UserAlreadyLinkedToProfessionalException();
        }

        User user = userService.findEntityById(userId);

        if (user.getRole() != Role.DOCTOR) {
            throw new BusinessRuleException(
                    "Only users with the DOCTOR role can be linked to a professional"
            );
        }

        return user;
    }

    private void ensureSpecialtyIsActive(Specialty specialty) {
        if (!specialty.isActive()) {
            throw new BusinessRuleException("Specialty must be active");
        }
    }

    private void ensureRegistrationIsAvailable(
            String registrationNumber,
            String registrationState,
            UUID professionalId
    ) {
        boolean inUse = professionalId == null
                ? professionalRepository
                        .existsByRegistrationNumberIgnoreCaseAndRegistrationStateIgnoreCase(
                                registrationNumber,
                                registrationState
                        )
                : professionalRepository
                        .existsByRegistrationNumberIgnoreCaseAndRegistrationStateIgnoreCaseAndIdNot(
                                registrationNumber,
                                registrationState,
                                professionalId
                        );

        if (inUse) {
            throw new RegistrationAlreadyInUseException(
                    registrationNumber,
                    registrationState
            );
        }
    }

    private Specification<Professional> buildSpecification(
            String search,
            UUID specialtyId,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), term),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("registrationNumber")),
                                term
                        )
                ));
            }

            if (specialtyId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("specialty").get("id"),
                        specialtyId
                ));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Pageable limit(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                pageable.getSort()
        );
    }
}
