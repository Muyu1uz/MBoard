package com.muyulu.mboard.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class AlbumCacheProperties {

    private Duration albumDetailRedisTtl = Duration.ofMinutes(30);
}
