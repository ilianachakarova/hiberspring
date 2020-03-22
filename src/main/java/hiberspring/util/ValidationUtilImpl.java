package hiberspring.util;

import javax.validation.Validation;
import javax.validation.Validator;

public class ValidationUtilImpl implements ValidationUtil {

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Override
    public <E> boolean isValid(E entity) {
        return validator.validate(entity).size() == 0;
    }
}
