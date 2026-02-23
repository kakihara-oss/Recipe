package com.recipe.manager.repository;

import com.recipe.manager.entity.AiConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConsultationMessageRepository extends JpaRepository<AiConsultationMessage, Long> {

    List<AiConsultationMessage> findByThreadIdOrderByCreatedAtAsc(Long threadId);
}
