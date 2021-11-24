package com.marcosmoreira.webflux.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CustomAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        Throwable throwable = getError(request);

        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException exception = (ResponseStatusException) throwable;
            errorAttributes.put("message", exception.getMessage());
            errorAttributes.put("developerMessage", "A ResponseStatusException Happened");
        }

        return errorAttributes;
    }
}