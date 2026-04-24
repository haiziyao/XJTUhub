package org.xjtuhub.admin;

import jakarta.validation.constraints.Size;

import java.time.Instant;

record AdminCampusVerificationRequest(
        @Size(max = 255) String note
) {
}

record AdminCampusVerificationResponse(
        String userId,
        String authLevel,
        String verificationStatus,
        Instant verifiedAt
) {
}
