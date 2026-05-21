package com.muyulu.mboard.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String baseDir = "uploads";
    private String publicBaseUrl = "http://localhost:8080/uploads";
}
