package io.github.officemed.medical_office_api.appointment.repository;

import io.github.officemed.medical_office_api.appointment.entity.Appointment;
import io.github.officemed.medical_office_api.appointment.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID>,
        JpaSpecificationExecutor<Appointment> {

    @Override
    @EntityGraph(attributePaths = {"patient", "professional", "service"})
    Page<Appointment> findAll(
            Specification<Appointment> specification,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"patient", "professional", "service"})
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.scheduledStart >= :start
              AND appointment.status IN :statuses
            ORDER BY appointment.scheduledStart
            """)
    List<Appointment> findUpcoming(
            @Param("start") OffsetDateTime start,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT appointment FROM Appointment appointment WHERE appointment.id = :appointmentId")
    Optional<Appointment> findByIdForUpdate(@Param("appointmentId") UUID appointmentId);

    @Query("""
            SELECT (COUNT(appointment) > 0)
            FROM Appointment appointment
            WHERE appointment.professional.id = :professionalId
              AND appointment.status IN :blockingStatuses
              AND appointment.scheduledStart < :scheduledEnd
              AND appointment.scheduledEnd > :scheduledStart
            """)
    boolean existsConflict(
            @Param("professionalId") UUID professionalId,
            @Param("scheduledStart") OffsetDateTime scheduledStart,
            @Param("scheduledEnd") OffsetDateTime scheduledEnd,
            @Param("blockingStatuses") Collection<AppointmentStatus> blockingStatuses
    );

    @Query("""
            SELECT (COUNT(appointment) > 0)
            FROM Appointment appointment
            WHERE appointment.professional.id = :professionalId
              AND appointment.id <> :appointmentId
              AND appointment.status IN :blockingStatuses
              AND appointment.scheduledStart < :scheduledEnd
              AND appointment.scheduledEnd > :scheduledStart
            """)
    boolean existsConflictExcluding(
            @Param("professionalId") UUID professionalId,
            @Param("appointmentId") UUID appointmentId,
            @Param("scheduledStart") OffsetDateTime scheduledStart,
            @Param("scheduledEnd") OffsetDateTime scheduledEnd,
            @Param("blockingStatuses") Collection<AppointmentStatus> blockingStatuses
    );

    long countByScheduledStartGreaterThanEqualAndScheduledStartLessThan(
            OffsetDateTime start,
            OffsetDateTime end
    );

    long countByScheduledStartGreaterThanEqualAndScheduledStartLessThanAndStatus(
            OffsetDateTime start,
            OffsetDateTime end,
            AppointmentStatus status
    );

    long countByScheduledStartGreaterThanEqualAndScheduledStartLessThanAndStatusIn(
            OffsetDateTime start,
            OffsetDateTime end,
            Collection<AppointmentStatus> statuses
    );
}
