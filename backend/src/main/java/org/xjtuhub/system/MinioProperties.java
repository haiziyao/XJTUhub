package org.xjtuhub.system;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xjtuhub.minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName
) {
}
