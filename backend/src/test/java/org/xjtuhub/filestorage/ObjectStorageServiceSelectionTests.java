package org.xjtuhub.filestorage;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.xjtuhub.system.MinioProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Import(ObjectStorageServiceSelectionTests.MinioStorageTestConfiguration.class)
class ObjectStorageServiceSelectionTests {
    @Autowired
    private ObjectStorageService objectStorageService;

    @Test
    void minioAdapterIsPreferredWhenMinioClientExists() {
        assertThat(objectStorageService.getClass().getSimpleName()).isEqualTo("MinioObjectStorageAdapter");
    }

    @TestConfiguration
    static class MinioStorageTestConfiguration {
        @Bean
        MinioClient minioClient() {
            return mock(MinioClient.class);
        }

        @Bean
        MinioProperties minioProperties() {
            return new MinioProperties("http://localhost:9000", "access", "secret", "xjtuhub-test");
        }
    }
}
