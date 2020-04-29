package com.singhand.bloomFilter.aspect;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
public class LogAspect {

    private static final Logger LOGGER = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Pointcut(value = "execution(* com.singhand.bloomFilter.controller..*(..))")
    public void requestServer(){}

    @Around("requestServer()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String protocol = request.getProtocol();
        String remoteAddr = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        long beginTimeMillis = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        LOGGER.info("{} ==>> \"{} {}{} {}\" {}ms <<== {}",
                Optional.ofNullable(remoteAddr).orElse(StringUtils.EMPTY),
                Optional.ofNullable(method).orElse(StringUtils.EMPTY),
                Optional.ofNullable(requestURI).orElse(StringUtils.EMPTY),
                "(" + Joiner.on(", ").withKeyValueSeparator("=").join(getRequestParams(proceedingJoinPoint)) + ")",
                Optional.ofNullable(protocol).orElse(StringUtils.EMPTY),
                String.valueOf(System.currentTimeMillis() - beginTimeMillis),
                JSON.toJSONString(result)
                );

        return result;
    }



    private Map<String, Object> getRequestParams(ProceedingJoinPoint proceedingJoinPoint) {
        Map<String, Object> requestParams = new HashMap<>();
        String[] paramNames =
                ((MethodSignature)proceedingJoinPoint.getSignature()).getParameterNames();
        Object[] paramValues = proceedingJoinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            Object value = paramValues[i];

            if (value instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) value;
                value = file.getOriginalFilename();
            }
            if(!Objects.isNull(value)){
                requestParams.put(paramNames[i], value);
            }
        }
        return requestParams;
    }

}
