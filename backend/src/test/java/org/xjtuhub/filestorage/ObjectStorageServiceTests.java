package org.xjtuhub.filestorage;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectStorageServiceTests {
    @Test
    void inMemoryStorageCreatesStableUploadAndDownloadUrls() {
        ObjectStorageService storageService = new InMemoryObjectStorageService();

        PresignedObjectUrl uploadUrl = storageService.createUploadUrl(new ObjectUploadRequest(
                "resources/intro.pdf",
                "application/pdf",
                1024L,
                Duration.ofMinutes(10)
        ));
        storageService.completeUpload("resources/intro.pdf");
        PresignedObjectUrl downloadUrl = storageService.getDownloadUrl("resources/intro.pdf", Duration.ofMinutes(5));

        assertThat(uploadUrl.method()).isEqualTo("PUT");
        assertThat(uploadUrl.url()).contains("/objects/resources/intro.pdf");
        assertThat(downloadUrl.method()).isEqualTo("GET");
        assertThat(downloadUrl.url()).contains("/objects/resources/intro.pdf");
        assertThat(storageService.getObjectMetadata("resources/intro.pdf").objectKey()).isEqualTo("resources/intro.pdf");
    }
}
