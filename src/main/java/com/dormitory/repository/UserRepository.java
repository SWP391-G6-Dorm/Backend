package com.dormitory.repository;

import com.dormitory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    long countByRole(User.Role role);
    
    java.util.Optional<User> findByEmail(String email);

}
