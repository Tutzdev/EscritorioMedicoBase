package io.github.officemed.medical_office_api.specialty.service;

import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import io.github.officemed.medical_office_api.specialty.dto.CreateSpecialtyRequest;
import io.github.officemed.medical_office_api.specialty.dto.SpecialtyResponse;
import io.github.officemed.medical_office_api.specialty.dto.UpdateSpecialtyRequest;
import io.github.officemed.medical_office_api.specialty.dto.UpdateSpecialtyStatusRequest;
import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import io.github.officemed.medical_office_api.specialty.exception.SpecialtyNameAlreadyInUseException;
import io.github.officemed.medical_office_api.specialty.exception.SpecialtyNotFoundException;
import io.github.officemed.medical_office_api.specialty.mapper.SpecialtyMapper;
import io.github.officemed.medical_office_api.specialty.repository.SpecialtyRepository;
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
public class SpecialtyService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    public SpecialtyService(
            SpecialtyRepository specialtyRepository,
            SpecialtyMapper specialtyMapper
    ) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyMapper = specialtyMapper;
    }

    @Transactional
    public SpecialtyResponse create(CreateSpecialtyRequest request) {
        ensureNameIsAvailable(request.name(), null);

        Specialty specialty = Specialty.create(
                request.name(),
                request.description()
        );

        return specialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Transactional(readOnly = true)
    public PageResponse<SpecialtyResponse> findAll(
            String search,
            Boolean active,
            Pageable pageable
    ) {
        Page<SpecialtyResponse> specialties = specialtyRepository
                .findAll(buildSpecification(search, active), limit(pageable))
                .map(specialtyMapper::toResponse);

        return PageResponse.from(specialties);
    }

    @Transactional(readOnly = true)
    public SpecialtyResponse findById(UUID specialtyId) {
        return specialtyMapper.toResponse(findEntityById(specialtyId));
    }

    @Transactional
    public SpecialtyResponse update(
            UUID specialtyId,
            UpdateSpecialtyRequest request
    ) {
        Specialty specialty = findEntityById(specialtyId);
        ensureNameIsAvailable(request.name(), specialtyId);
        specialty.update(request.name(), request.description());
        return specialtyMapper.toResponse(specialty);
    }

    @Transactional
    public SpecialtyResponse updateStatus(
            UUID specialtyId,
            UpdateSpecialtyStatusRequest request
    ) {
        Specialty specialty = findEntityById(specialtyId);

        if (request.active()) {
            specialty.activate();
        } else {
            specialty.deactivate();
        }

        return specialtyMapper.toResponse(specialty);
    }

    @Transactional(readOnly = true)
    public Specialty findEntityById(UUID specialtyId) {
        return specialtyRepository
                .findById(specialtyId)
                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));
    }

    private void ensureNameIsAvailable(
            String name,
            UUID specialtyId
    ) {
        boolean inUse = specialtyId == null
                ? specialtyRepository.existsByNameIgnoreCase(name.trim())
                : specialtyRepository.existsByNameIgnoreCaseAndIdNot(
                        name.trim(),
                        specialtyId
                );

        if (inUse) {
            throw new SpecialtyNameAlreadyInUseException(name);
        }
    }

    private Specification<Specialty> buildSpecification(
            String search,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        term
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
