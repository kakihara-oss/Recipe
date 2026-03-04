package com.recipe.manager.repository;

import com.recipe.manager.entity.ServiceDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceDesignRepository extends JpaRepository<ServiceDesign, Long> {

    Optional<ServiceDesign> findById(Long id);
}
