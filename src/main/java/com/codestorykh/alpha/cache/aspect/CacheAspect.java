package com.codestorykh.alpha.cache.aspect;

import com.codestorykh.alpha.cache.annotation.DynamicCache;
import com.codestorykh.alpha.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {

    private final CacheService cacheService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(dynamicCache)")
    public Object around(ProceedingJoinPoint joinPoint, DynamicCache dynamicCache) throws Throwable {
        Method method = getMethod(joinPoint);
        String cacheKey = generateCacheKey(joinPoint, method, dynamicCache);
        String cacheName = dynamicCache.cacheName();
        Duration ttl = Duration.of(dynamicCache.ttl(), dynamicCache.timeUnit().toChronoUnit());
        Class<?> returnType = method.getReturnType();

        // Check if return type is Optional
        boolean isOptional = returnType.equals(Optional.class);

        // Try to get from cache first
        if (!dynamicCache.refresh()) {
            if (isOptional) {
                // For Optional types, we need to determine the actual type inside the Optional
                // Since we can't easily get the generic type, we'll use Object.class and handle conversion
                Optional<Object> cachedValue = cacheService.get(cacheName, cacheKey, Object.class);
                if (cachedValue.isPresent()) {
                    Object value = cachedValue.get();
                    log.debug("Cache hit for key: {} in cache: {} (Optional type)", cacheKey, cacheName);
                    log.debug("Cached value type: {}", value.getClass().getSimpleName());
                    
                    // If the value is a Map (LinkedHashMap), we need to convert it to User
                    if (value instanceof Map) {
                        try {
                            // Try to convert to User first (most common case)
                            value = cacheService.getRedisObjectMapper().convertValue(value, com.codestorykh.alpha.identity.domain.User.class);
                            log.debug("Converted Map to User object");
                        } catch (Exception e) {
                            log.warn("Failed to convert cached Map to User for key: {}. Will execute method instead.", cacheKey);
                            // If conversion fails, don't return the cached value, execute the method instead
                            return executeMethodAndCache(joinPoint, dynamicCache, cacheKey, cacheName, ttl, isOptional);
                        }
                    }
                    
                    return Optional.of(value);
                }
            } else {
                Optional<?> cachedValue = cacheService.get(cacheName, cacheKey, returnType);
                if (cachedValue.isPresent()) {
                    log.debug("Cache hit for key: {} in cache: {}", cacheKey, cacheName);
                    return cachedValue.get();
                }
            }
        }

        return executeMethodAndCache(joinPoint, dynamicCache, cacheKey, cacheName, ttl, isOptional);
    }

    private Object executeMethodAndCache(ProceedingJoinPoint joinPoint, DynamicCache dynamicCache, 
                                       String cacheKey, String cacheName, Duration ttl, boolean isOptional) throws Throwable {
        // Execute the method
        Object result = joinPoint.proceed();

        // Cache the result
        if (result != null || dynamicCache.cacheNull()) {
            if (isOptional && result instanceof Optional<?> optionalResult) {
                // For Optional types, cache the actual value if present
                if (optionalResult.isPresent()) {
                    cacheService.set(cacheName, cacheKey, optionalResult.get(), ttl);
                    log.debug("Cached Optional value for key: {} in cache: {}", cacheKey, cacheName);
                }
            } else {
                cacheService.set(cacheName, cacheKey, result, ttl);
                log.debug("Cached result for key: {} in cache: {}", cacheKey, cacheName);
            }
        }

        return result;
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        String methodName = joinPoint.getSignature().getName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Class<?>[] parameterTypes = new Class[joinPoint.getArgs().length];
        
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            parameterTypes[i] = joinPoint.getArgs()[i] != null ? joinPoint.getArgs()[i].getClass() : Object.class;
        }
        
        return targetClass.getMethod(methodName, parameterTypes);
    }

    private String generateCacheKey(ProceedingJoinPoint joinPoint, Method method, DynamicCache dynamicCache) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // Add key prefix
        if (!dynamicCache.keyPrefix().isEmpty()) {
            keyBuilder.append(dynamicCache.keyPrefix()).append(":");
        }
        
        // Add cache name
        keyBuilder.append(dynamicCache.cacheName()).append(":");
        
        // Add method name
        keyBuilder.append(method.getName()).append(":");
        
        // Add custom key expression or method parameters
        if (!dynamicCache.key().isEmpty()) {
            String customKey = evaluateExpression(dynamicCache.key(), method, joinPoint.getArgs());
            keyBuilder.append(customKey);
        } else {
            // Use method parameters as key
            Object[] args = joinPoint.getArgs();
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
            
            if (paramNames != null) {
                for (int i = 0; i < args.length && i < paramNames.length; i++) {
                    if (i > 0) keyBuilder.append(":");
                    keyBuilder.append(paramNames[i]).append("=").append(args[i]);
                }
            } else {
                // Fallback to parameter values only
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) keyBuilder.append(":");
                    keyBuilder.append(args[i]);
                }
            }
        }
        
        return keyBuilder.toString();
    }

    private String evaluateExpression(String expression, Method method, Object[] args) {
        try {
            Expression exp = expressionParser.parseExpression(expression);
            EvaluationContext context = new StandardEvaluationContext();
            
            // Add method parameters to context
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < args.length && i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            
            Object result = exp.getValue(context);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            log.warn("Failed to evaluate expression: {}", expression, e);
            return "";
        }
    }
} 