package com.homestay.repository;

import com.homestay.entity.MaintenanceTicket;
import com.homestay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, UUID> {
    List<MaintenanceTicket> findByCustomerOrderByCreatedAtDesc(User customer);
    List<MaintenanceTicket> findAllByOrderByCreatedAtDesc();

    long countByCustomerIdAndStatusIn(UUID customerId, java.util.Collection<MaintenanceTicket.Status> statuses);

    @Query("""
        SELECT COUNT(m) FROM MaintenanceTicket m
        JOIN m.room r
        WHERE r.property.id = :propertyId
          AND m.status = 'OPEN'
        """)
    long countOpenByPropertyId(@Param("propertyId") UUID propertyId);
}
