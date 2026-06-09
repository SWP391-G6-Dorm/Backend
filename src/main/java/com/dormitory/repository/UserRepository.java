package com.dormitory.repository;

import com.dormitory.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    long countByRole(User.Role role);
    
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%',:keyword,'%')) " +
           " OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<User> searchUsers(
        @Param("role") User.Role role,
        @Param("status") User.Status status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
