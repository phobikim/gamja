package com.phobi.gamja.web.config;

import com.phobi.gamja.web.config.annotation.SanitizeInput;
import com.phobi.gamja.util.SecurityUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.Map;

@Aspect
@Component
public class InputSanitizerAspect {

    @Around("@annotation(sanitizeInput)")
    public Object sanitizeAndProceed(ProceedingJoinPoint pjp, SanitizeInput sanitizeInput) throws Throwable {
        Object[] args = pjp.getArgs();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String str) {
                args[i] = SecurityUtil.sanitize(str); // 🔥 여기서 실제로 치환됨
            } else if (arg instanceof Map<?, ?> map) {
                sanitizeMap((Map<String, Object>) map);
            } else {
                sanitizeObject(arg);
            }
        }

        return pjp.proceed(args);
    }

    private void sanitizeMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String str) {
                entry.setValue(SecurityUtil.sanitize(str));
            } else if (val instanceof Map<?, ?> nestedMap) {
                sanitizeMap((Map<String, Object>) nestedMap);
            } else {
                sanitizeObject(val);
            }
        }
    }

    private void sanitizeObject(Object obj) {
        if (obj == null) return;

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        field.set(obj, SecurityUtil.sanitize(value));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("XSS 필터링 실패", e);
                }
            }
        }
    }
}
