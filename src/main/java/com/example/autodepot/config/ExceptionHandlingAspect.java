package com.example.autodepot.config;

import com.example.autodepot.exception.ExceptionCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Wraps all application methods with try/catch and records exceptions centrally.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExceptionHandlingAspect {

    private final ExceptionCollector exceptionCollector;

    public ExceptionHandlingAspect(ExceptionCollector exceptionCollector) {
        this.exceptionCollector = exceptionCollector;
    }

    @Around("execution(* com.example.autodepot..*(..)) && !within(com.example.autodepot.exception..*)")
    public Object wrapAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            exceptionCollector.record(ex, joinPoint.getSignature().toShortString());
            throw ex;
        }
    }
}
