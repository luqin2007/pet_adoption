package com.example.backend.dto;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * 请求检查接口<br>
 * -- 对应类：Request，Param，Table<br>
 * -- 只检查字段值，不检查数据库
 */
public interface IValidatedRequest extends IRequest {

    void validate(Errors errors);

    default <T, E extends Enum<E>> void validateEnum(Errors errors, SFunction<T, String> fieldRef, Class<E> enumClass, String errorCode) {
        FieldWrapper<String> field = getNameAndValue(fieldRef);
        if (field.isEmpty()) return;

        try {
            Enum.valueOf(enumClass, field.value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            errors.rejectValue(field.name, errorCode);
        }
    }

    default <T, E extends Enum<E>> void validateEnums(Errors errors, SFunction<T, Collection<String>> fieldRef, Class<E> enumClass, String errorCode) {
        FieldWrapper<Collection<String>> field = getNameAndValue(fieldRef);
        if (field.isEmpty() || field.value.isEmpty()) return;

        try {
            for (String value : field.value) {
                Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
            }
        } catch (IllegalArgumentException e) {
            errors.rejectValue(field.name, errorCode);
        }
    }

    default <T> void validateTime(Errors errors, SFunction<T, Date> time0Ref, SFunction<T, Date> time1Ref) {
        var time0 = getNameAndValue(time0Ref);
        var time1 = getNameAndValue(time1Ref);
        if (!time0.isEmpty() && !time1.isEmpty() && time0.value.after(time1.value))
            errors.rejectValue(time0.name, "request.date_range", "时间范围错误");
    }

    default <T> void validateDependency(Errors errors, SFunction<T, ?> fieldRef, Object dependency) {
        FieldWrapper<?> field = getNameAndValue(fieldRef);
        if (field.value != null && dependency == null)
            errors.rejectValue(field.name, "request.dependency");
    }

    default <T> void validateNumber(Errors errors, SFunction<T, String> fieldRef, String errorCode) {
        FieldWrapper<String> field = getNameAndValue(fieldRef);
        if (!field.isEmpty() && !field.value.matches("^\\d+(\\.\\d{1,2})?$"))
            errors.rejectValue(field.name, errorCode);
    }

    default <T> void validateRange(Errors errors, SFunction<T, String> fieldRef0, SFunction<T, String> fieldRef1, String errorCode) {
        FieldWrapper<String> field0 = getNameAndValue(fieldRef0);
        FieldWrapper<String> field1 = getNameAndValue(fieldRef1);
        if (!field0.isEmpty() && !field0.value.matches("^\\d+(\\.\\d{1,2})?$"))
            errors.rejectValue(field0.name, errorCode);
        if (!field1.isEmpty() && !field1.value.matches("^\\d+(\\.\\d{1,2})?$"))
            errors.rejectValue(field1.name, errorCode);
        if (!field0.isEmpty() && !field1.isEmpty()) {
            BigDecimal value0 = new BigDecimal(field0.value);
            BigDecimal value1 = new BigDecimal(field1.value);
            if (value0.compareTo(value1) > 0)
                errors.rejectValue(field0.name, errorCode);
        }
    }

    default <T> void validateIntRange(Errors errors, SFunction<T, Integer> fieldRef0, SFunction<T, Integer> fieldRef1, String errorCode) {
        FieldWrapper<Integer> field0 = getNameAndValue(fieldRef0);
        FieldWrapper<Integer> field1 = getNameAndValue(fieldRef1);
        if (!field0.isEmpty() && !field1.isEmpty()) {
            if (field0.value.compareTo(field1.value) > 0)
                errors.rejectValue(field0.name, errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    default <T> void validateOne(Errors errors, SFunction<T, ?>... params) {
        boolean first = false;
        String second = null;
        for (SFunction<T, ?> param : params) {
            FieldWrapper<?> field = getNameAndValue(param);
            if (!field.isEmpty()) {
                if (first) {
                    second = field.name;
                    break;
                } else {
                    first = true;
                }
            }
        }
        if (second != null) {
            errors.rejectValue(second, "request.mutex");
        }
    }

    private <T, O> FieldWrapper<O> getNameAndValue(SFunction<T, O> getter) {
        LambdaMeta extract = LambdaUtils.extract(getter);
        String methodName = extract.getImplMethodName(); // getXxx
        String fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        return new FieldWrapper<>(fieldName, getter.apply(self()));
    }

    record FieldWrapper<T>(String name, T value) {
        boolean isEmpty() {
            return value == null;
        }
    }
}
