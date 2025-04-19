package com.dipper.monitor.config.excep;

import com.dipper.common.lib.dubbo.DubboResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DubboResult<Void> handleException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return DubboResult.buildErrorResult("999999", "系统错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DubboResult<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Bad Request: {}", ex.getMessage(), ex);
        return DubboResult.buildErrorResult("999999", "非法请求");
    }


}