package org.xjtuhub.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.xjtuhub.common.validation.HttpUrlOrBlank;

public record UpdateCurrentUserRequest(
        @NotBlank @Size(max = 64) String nickname,
        @Size(max = 512) String bio,
        @Size(max = 512) @HttpUrlOrBlank String avatarUrl
) {
}
