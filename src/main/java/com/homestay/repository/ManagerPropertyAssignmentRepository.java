package com.homestay.repository;

import com.homestay.entity.ManagerPropertyAssignment;
import com.homestay.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManagerPropertyAssignmentRepository extends JpaRepository<ManagerPropertyAssignment, UUID> {

    boolean existsByManagerIdAndPropertyIdAndStatus(
            UUID managerId, UUID propertyId, ManagerPropertyAssignment.Status status);

    @Query("""
        SELECT mpa.property FROM ManagerPropertyAssignment mpa
        WHERE mpa.manager.id = :managerId
          AND mpa.status = 'ACTIVE'
          AND mpa.property.status = 'ACTIVE'
        ORDER BY mpa.property.name ASC
        """)
    List<Property> findActivePropertiesByManagerId(@Param("managerId") UUID managerId);
}
