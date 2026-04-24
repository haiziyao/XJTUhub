package org.xjtuhub.auth;

import org.junit.jupiter.api.Test;
import org.xjtuhub.common.support.LocalIdGenerator;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CampusScanStoreTests {
    @Test
    void inMemoryStoreCreatesAndUpdatesCampusScanSession() {
        CampusScanStore store = new InMemoryCampusScanStore(new LocalIdGenerator());
        Instant now = Instant.parse("2026-04-24T03:00:00Z");
        Instant expiresAt = Instant.parse("2026-04-24T03:05:00Z");

        CampusScanStore.StoredCampusScanSession created = store.createSession("scene-1", "hash-1", expiresAt, now);

        assertThat(created.sceneId()).isEqualTo("scene-1");
        assertThat(created.qrTokenHash()).isEqualTo("hash-1");
        assertThat(created.status()).isEqualTo("pending");
        assertThat(created.expiresAt()).isEqualTo(expiresAt);

        store.markScanned("scene-1", 1776999849060006L, now.plusSeconds(10));
        CampusScanStore.StoredCampusScanSession scanned = store.findBySceneId("scene-1").orElseThrow();
        assertThat(scanned.status()).isEqualTo("scanned");
        assertThat(scanned.matchedUserId()).isEqualTo(1776999849060006L);
        assertThat(scanned.scannedAt()).isEqualTo(now.plusSeconds(10));

        store.markConfirmed("scene-1", now.plusSeconds(20));
        CampusScanStore.StoredCampusScanSession confirmed = store.findBySceneId("scene-1").orElseThrow();
        assertThat(confirmed.status()).isEqualTo("confirmed");
        assertThat(confirmed.confirmedAt()).isEqualTo(now.plusSeconds(20));
    }
}
