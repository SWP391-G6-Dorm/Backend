package com.homestay.repository;



import com.homestay.entity.HousekeepingTask;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;



import java.time.LocalDateTime;

import java.util.Collection;

import java.util.List;

import java.util.Optional;

import java.util.UUID;



public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, UUID> {



    @Query("""

            SELECT t FROM HousekeepingTask t

            JOIN FETCH t.room

            LEFT JOIN FETCH t.assignedEmployee

            WHERE t.property.id = :propertyId

              AND t.status IN :statuses

              AND t.createdAt >= :start

              AND t.createdAt < :end

            ORDER BY t.createdAt ASC

            """)

    List<HousekeepingTask> findScheduleTasks(

            @Param("propertyId") UUID propertyId,

            @Param("statuses") List<HousekeepingTask.Status> statuses,

            @Param("start") LocalDateTime start,

            @Param("end") LocalDateTime end);



    @Query(

            value = """

            SELECT t FROM HousekeepingTask t

            WHERE t.property.id = :propertyId

              AND (:status IS NULL OR t.status = :status)

              AND t.createdAt >= :fromDate

              AND t.createdAt < :toDate

            ORDER BY t.createdAt DESC

            """,

            countQuery = """

            SELECT COUNT(t) FROM HousekeepingTask t

            WHERE t.property.id = :propertyId

              AND (:status IS NULL OR t.status = :status)

              AND t.createdAt >= :fromDate

              AND t.createdAt < :toDate

            """)

    Page<HousekeepingTask> findForManagerBoard(

            @Param("propertyId") UUID propertyId,

            @Param("status") HousekeepingTask.Status status,

            @Param("fromDate") LocalDateTime fromDate,

            @Param("toDate") LocalDateTime toDate,

            Pageable pageable);



    boolean existsByBooking_IdAndStatusIn(UUID bookingId, Collection<HousekeepingTask.Status> statuses);



    @Query("""
            SELECT t FROM HousekeepingTask t
            WHERE t.room.id = :roomId
              AND t.status IN :statuses
            """)
    Optional<HousekeepingTask> findOpenByRoomId(
            @Param("roomId") UUID roomId,
            @Param("statuses") Collection<HousekeepingTask.Status> statuses);

}

