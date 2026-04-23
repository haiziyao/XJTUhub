package org.xjtuhub.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SnowflakeIdValidator implements ConstraintValidator<SnowflakeId, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches("\\d+");
    }
}
