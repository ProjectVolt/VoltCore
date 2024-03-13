package io.github.capure.voltcore.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {
    private Pattern pattern;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        pattern = Pattern.compile("^ROLE_(USER|STUFF|ADMIN)$");
    }

    @Override
    public boolean isValid(String role, ConstraintValidatorContext context) {
        if (role == null) {
            return true; // Consider null values as valid
        }
        return pattern.matcher(role).matches();
    }
}