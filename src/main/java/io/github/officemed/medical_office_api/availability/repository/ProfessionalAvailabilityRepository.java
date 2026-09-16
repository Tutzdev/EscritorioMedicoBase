package io.github.officemed.medical_office_api.availability.repository;

import io.github.officemed.medical_office_api.availability.entity.ProfessionalAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository
        extends JpaRepository<ProfessionalAvailability, UUID> {

    Page<ProfessionalAvailability> findByProfessionalId(
            UUID professionalId,
            Pageable pageable
    );

    @Query("""
            SELECT (COUNT(availability) > 0)
            FROM ProfessionalAvailability availability
            WHERE availability.professional.id = :professionalId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.active = true
              AND availability.startTime < :endTime
              AND availability.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("professionalId") UUID professionalId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT (COUNT(availability) > 0)
            FROM ProfessionalAvailability availability
            WHERE availability.professional.id = :professionalId
              AND availability.id <> :availabilityId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.active = true
              AND availability.startTime < :endTime
              AND availability.endTime > :startTime
            """)
    boolean existsOverlappingExcluding(
            @Param("professionalId") UUID professionalId,
            @Param("availabilityId") UUID availabilityId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT (COUNT(availability) > 0)
            FROM ProfessionalAvailability availability
            WHERE availability.professional.id = :professionalId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.active = true
              AND availability.startTime <= :startTime
              AND availability.endTime >= :endTime
            """)
    boolean existsCovering(
            @Param("professionalId") UUID professionalId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
