package org.xjtuhub.admin;

import jakarta.validation.constraints.Size;
import org.xjtuhub.common.api.IdentityBindingDto;

import java.time.Instant;
import java.util.List;

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

record CurrentAdminResponse(
        String adminAccountId,
        String userId,
        String adminRole,
        String status
) {
}

record AdminAuditLogDto(
        String id,
        String actorUserId,
        String adminAccountId,
        String action,
        String targetType,
        String targetId,
        String requestId,
        String ipHash,
        String userAgentHash,
        String detailsJson,
        Instant createdAt
) {
}

record AdminUserIdentityBindingsResponse(
        String userId,
        List<IdentityBindingDto> identityBindings
) {
}
