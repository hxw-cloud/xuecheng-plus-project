package com.hxw.base.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doXueChengPlusException(XueChengPlusException e) {
        log.error("捕获的异常信息{}", e.getErrMessage());
        e.printStackTrace();

        String message = e.getErrMessage();
        return new RestErrorResponse(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        StringBuffer buffer = new StringBuffer();
        List<FieldError> fieldErrors = result.getFieldErrors();
        fieldErrors.forEach(error -> {
            buffer.append(error.getDefaultMessage()).append(",");
        });

        return new RestErrorResponse(buffer.toString());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doException(Exception e) {
        log.error("捕获的异常信息{}", e.getMessage());
        e.printStackTrace();

        String message = e.getMessage();
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}
