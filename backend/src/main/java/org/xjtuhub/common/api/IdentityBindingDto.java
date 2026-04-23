package org.xjtuhub.common.api;

public record IdentityBindingDto(
        String provider,
        String providerDisplay,
        String verificationStatus,
        boolean primary,
        boolean lastUsed
) {
}
