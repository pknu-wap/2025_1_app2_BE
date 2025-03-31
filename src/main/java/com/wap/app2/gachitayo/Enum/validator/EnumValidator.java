package com.wap.app2.gachitayo.Enum.validator;

import com.wap.app2.gachitayo.Enum.LocationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, LocationType> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(LocationType value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        return Arrays.stream(LocationType.values())
                .anyMatch(enumValue -> enumValue == value);
    }
}
