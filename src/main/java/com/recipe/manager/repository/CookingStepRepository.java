package com.recipe.manager.repository;

import com.recipe.manager.entity.CookingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CookingStepRepository extends JpaRepository<CookingStep, Long> {

    Optional<CookingStep> findById(Long id);
}
