package org.xjtuhub.common.api;

public record BadgeDto(
        String type,
        String code,
        String label,
        String tone
) {
}
