package com.homestay.repository;

import com.homestay.entity.AboutContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AboutContentRepository extends JpaRepository<AboutContent, UUID> {

    Optional<AboutContent> findBySingletonKey(String singletonKey);
}
