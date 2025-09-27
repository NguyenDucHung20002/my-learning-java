package com.my.demo.response;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class FormatApiRestResponse implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // Apply to all responses
        return true;
    }
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response)
    {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        ApiResponse<Object> apiResponse = new ApiResponse<Object>();
        apiResponse.setStatusCode(status);

        if (body instanceof ApiResponse) {
            return body;
        } else if (body instanceof String) {
            // Special handling for String responses
            // to avoid issues with message converters
           return body;

        }

        // Check for custom message annotation
        String customMessage = null;
        if (returnType.getMethod() != null && returnType.getMethod().isAnnotationPresent(ApiMessage.class)) {
            customMessage = returnType.getMethod().getAnnotation(ApiMessage.class).value();
        } else if (returnType.getContainingClass().isAnnotationPresent(ApiMessage.class)) {
            customMessage = returnType.getContainingClass().getAnnotation(ApiMessage.class).value();
        }

        if(status >= 400) {
            apiResponse.setErrorCode("CALL_API_FAILED");
            apiResponse.setData(body);
            if (customMessage != null) apiResponse.setMessage(customMessage);
        }else{
            apiResponse.setMessage(customMessage != null ? customMessage : "CALL_API_SUCCESS");
            apiResponse.setData(body);
        }
        return apiResponse;

    }
    // Helper method to extract HTTP status from @ResponseStatus annotation

}