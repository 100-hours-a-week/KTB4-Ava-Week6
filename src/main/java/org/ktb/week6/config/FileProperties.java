package org.ktb.week6.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public record FileProperties(
        String uploadDir
) {
}
