package com.recipe.manager.repository;

import com.recipe.manager.entity.AiConsultationThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiConsultationThreadRepository extends JpaRepository<AiConsultationThread, Long> {

    Page<AiConsultationThread> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Page<AiConsultationThread> findByRecipeIdOrderByUpdatedAtDesc(Long recipeId, Pageable pageable);
}
