package com.recipe.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String allowedDomain = "example.com";

    private String frontendUrl = "http://localhost:3000";

    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Storage {
        private String provider = "local";
        private String localBasePath = "./uploads";
        private String googleDriveFolderId;
        private String googleDriveCredentialsJson;
    }
}
