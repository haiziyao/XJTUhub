package org.xjtuhub.filestorage;

import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xjtuhub.system.MinioProperties;

@Configuration
public class FileStorageConfiguration {
    @Bean
    ObjectStorageService objectStorageService(
            ObjectProvider<MinioClient> minioClientProvider,
            MinioProperties minioProperties,
            InMemoryObjectStorageService inMemoryObjectStorageService
    ) {
        MinioClient minioClient = minioClientProvider.getIfAvailable();
        if (minioClient != null) {
            return new MinioObjectStorageAdapter(minioClient, minioProperties);
        }
        return inMemoryObjectStorageService;
    }
}
