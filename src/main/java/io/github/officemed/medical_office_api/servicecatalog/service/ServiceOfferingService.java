package io.github.officemed.medical_office_api.servicecatalog.service;

import io.github.officemed.medical_office_api.servicecatalog.dto.CreateServiceRequest;
import io.github.officemed.medical_office_api.servicecatalog.dto.ServiceResponse;
import io.github.officemed.medical_office_api.servicecatalog.dto.UpdateServiceRequest;
import io.github.officemed.medical_office_api.servicecatalog.dto.UpdateServiceStatusRequest;
import io.github.officemed.medical_office_api.servicecatalog.entity.ServiceOffering;
import io.github.officemed.medical_office_api.servicecatalog.exception.ServiceNameAlreadyInUseException;
import io.github.officemed.medical_office_api.servicecatalog.exception.ServiceNotFoundException;
import io.github.officemed.medical_office_api.servicecatalog.mapper.ServiceOfferingMapper;
import io.github.officemed.medical_office_api.servicecatalog.repository.ServiceOfferingRepository;
import io.github.officemed.medical_office_api.shared.exception.BusinessRuleException;
import io.github.officemed.medical_office_api.shared.pagination.PageResponse;
import io.github.officemed.medical_office_api.specialty.entity.Specialty;
import io.github.officemed.medical_office_api.specialty.service.SpecialtyService;
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
public class ServiceOfferingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ServiceOfferingRepository serviceRepository;
    private final SpecialtyService specialtyService;
    private final ServiceOfferingMapper serviceMapper;

    public ServiceOfferingService(
            ServiceOfferingRepository serviceRepository,
            SpecialtyService specialtyService,
            ServiceOfferingMapper serviceMapper
    ) {
        this.serviceRepository = serviceRepository;
        this.specialtyService = specialtyService;
        this.serviceMapper = serviceMapper;
    }

    @Transactional
    public ServiceResponse create(CreateServiceRequest request) {
        Specialty specialty = specialtyService.findEntityById(request.specialtyId());
        ensureSpecialtyIsActive(specialty);
        ensureNameIsAvailable(request.specialtyId(), request.name(), null);

        ServiceOffering service = ServiceOffering.create(
                specialty,
                request.name(),
                request.description(),
                request.durationMinutes(),
                request.price()
        );

        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> findAll(
            String search,
            UUID specialtyId,
            Boolean active,
            Pageable pageable
    ) {
        Page<ServiceResponse> services = serviceRepository
                .findAll(
                        buildSpecification(search, specialtyId, active),
                        limit(pageable)
                )
                .map(serviceMapper::toResponse);

        return PageResponse.from(services);
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(UUID serviceId) {
        return serviceMapper.toResponse(findEntityById(serviceId));
    }

    @Transactional
    public ServiceResponse update(
            UUID serviceId,
            UpdateServiceRequest request
    ) {
        ServiceOffering service = findEntityById(serviceId);
        Specialty specialty = specialtyService.findEntityById(request.specialtyId());

        ensureSpecialtyIsActive(specialty);
        ensureNameIsAvailable(request.specialtyId(), request.name(), serviceId);
        service.update(
                specialty,
                request.name(),
                request.description(),
                request.durationMinutes(),
                request.price()
        );

        return serviceMapper.toResponse(service);
    }

    @Transactional
    public ServiceResponse updateStatus(
            UUID serviceId,
            UpdateServiceStatusRequest request
    ) {
        ServiceOffering service = findEntityById(serviceId);

        if (request.active()) {
            service.activate();
        } else {
            service.deactivate();
        }

        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public ServiceOffering findEntityById(UUID serviceId) {
        return serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    private void ensureSpecialtyIsActive(Specialty specialty) {
        if (!specialty.isActive()) {
            throw new BusinessRuleException("Specialty must be active");
        }
    }

    private void ensureNameIsAvailable(
            UUID specialtyId,
            String name,
            UUID serviceId
    ) {
        boolean inUse = serviceId == null
                ? serviceRepository.existsBySpecialtyIdAndNameIgnoreCase(
                        specialtyId,
                        name.trim()
                )
                : serviceRepository.existsBySpecialtyIdAndNameIgnoreCaseAndIdNot(
                        specialtyId,
                        name.trim(),
                        serviceId
                );

        if (inUse) {
            throw new ServiceNameAlreadyInUseException(name);
        }
    }

    private Specification<ServiceOffering> buildSpecification(
            String search,
            UUID specialtyId,
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
