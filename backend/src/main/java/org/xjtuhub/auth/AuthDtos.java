package org.xjtuhub.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.xjtuhub.common.api.BadgeDto;

import java.time.Instant;
import java.util.List;

record EmailTokenCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String purpose
) {
}

record EmailTokenCreateResponse(
        String email,
        String purpose,
        Instant expiresAt,
        String delivery,
        String token
) {
}

record EmailSessionCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String purpose,
        @NotBlank String token,
        String deviceLabel
) {
}

record AuthSessionDto(
        String id,
        String loginProvider,
        String deviceLabel,
        Instant createdAt,
        Instant expiresAt,
        boolean current
) {
}

record EmailSessionCreateResponse(
        CurrentUserDto user,
        AuthSessionDto session
) {
}

record SessionRevokeResponse(
        boolean revoked
) {
}
