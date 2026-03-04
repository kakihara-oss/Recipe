package com.recipe.manager.storage;

import com.recipe.manager.config.AppProperties;
import com.recipe.manager.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path basePath;

    public LocalFileStorageService(AppProperties appProperties) {
        this.basePath = Paths.get(appProperties.getStorage().getLocalBasePath());
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(basePath);
            log.info("Local file storage initialized at: {}", basePath.toAbsolutePath());
        } catch (IOException e) {
            throw new ApplicationException("ファイル保存ディレクトリの作成に失敗しました", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        try {
            Path target = basePath.resolve(storedFilename);
            file.transferTo(target);
            log.info("File stored locally: {}", target);
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new ApplicationException("ファイルの保存に失敗しました", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        String filename = fileUrl.substring("/uploads/".length());
        try {
            Path target = basePath.resolve(filename);
            Files.deleteIfExists(target);
            log.info("File deleted: {}", target);
        } catch (IOException e) {
            log.warn("ファイルの削除に失敗しました: {}", fileUrl, e);
        }
    }
}
