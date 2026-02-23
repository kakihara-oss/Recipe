package com.recipe.manager.dto.response;

import com.recipe.manager.entity.AiConsultationMessage;
import com.recipe.manager.entity.SenderType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AiMessageResponse {

    private final Long id;
    private final SenderType senderType;
    private final String content;
    private final List<ReferencedArticleInfo> referencedArticles;
    private final LocalDateTime createdAt;

    @Getter
    @Builder
    public static class ReferencedArticleInfo {
        private final Long id;
        private final String title;
    }

    public static AiMessageResponse from(AiConsultationMessage message) {
        List<ReferencedArticleInfo> refs = null;
        if (message.getReferencedArticles() != null && !message.getReferencedArticles().isEmpty()) {
            refs = message.getReferencedArticles().stream()
                    .map(a -> ReferencedArticleInfo.builder()
                            .id(a.getId())
                            .title(a.getTitle())
                            .build())
                    .toList();
        }

        return AiMessageResponse.builder()
                .id(message.getId())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .referencedArticles(refs)
                .createdAt(message.getCreatedAt())
                .build();
    }
}
