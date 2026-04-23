package org.xjtuhub.auth;

import org.xjtuhub.common.api.BadgeDto;
import org.xjtuhub.common.api.IdentityBindingDto;

import java.util.List;

public record CurrentUserDto(
        String id,
        String nickname,
        String avatarUrl,
        String bio,
        String authLevel,
        String nameColor,
        String primaryIdentityProvider,
        String lastLoginProvider,
        String identitySummary,
        List<IdentityBindingDto> identityBindings,
        List<BadgeDto> displayBadges
) {
}
