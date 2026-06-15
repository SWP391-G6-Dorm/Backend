package com.homestay.repository;

import com.homestay.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    // Tìm kiếm property theo tên (manager)
    Page<Property> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Lọc theo status
    Page<Property> findByStatus(Property.Status status, Pageable pageable);

    // Tìm theo tên và status
    Page<Property> findByNameContainingIgnoreCaseAndStatus(
            String name, Property.Status status, Pageable pageable);
}
