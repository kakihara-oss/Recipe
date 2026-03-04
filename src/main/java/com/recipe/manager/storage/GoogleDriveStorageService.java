package com.recipe.manager.storage;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.recipe.manager.config.AppProperties;
import com.recipe.manager.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "google-drive")
public class GoogleDriveStorageService implements FileStorageService {

    private final AppProperties appProperties;
    private Drive driveService;

    public GoogleDriveStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        try {
            String credentialsJson = appProperties.getStorage().getGoogleDriveCredentialsJson();
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));

            driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Recipe Manager")
                    .build();

            log.info("Google Drive storage initialized");
        } catch (IOException | GeneralSecurityException e) {
            throw new ApplicationException("Google Drive サービスの初期化に失敗しました", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(file.getOriginalFilename());
            String folderId = appProperties.getStorage().getGoogleDriveFolderId();
            if (folderId != null && !folderId.isBlank()) {
                fileMetadata.setParents(List.of(folderId));
            }

            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(), file.getInputStream());

            File uploaded = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink, webContentLink")
                    .execute();

            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            driveService.permissions().create(uploaded.getId(), permission).execute();

            String url = "https://drive.google.com/uc?id=" + uploaded.getId();
            log.info("File uploaded to Google Drive: id={}", uploaded.getId());
            return url;
        } catch (IOException e) {
            throw new ApplicationException("Google Driveへのファイルアップロードに失敗しました", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("id=")) {
            return;
        }
        String fileId = fileUrl.substring(fileUrl.indexOf("id=") + 3);
        try {
            driveService.files().delete(fileId).execute();
            log.info("File deleted from Google Drive: id={}", fileId);
        } catch (IOException e) {
            log.warn("Google Driveからのファイル削除に失敗しました: {}", fileUrl, e);
        }
    }
}
