package com.flow.common.exception;

import com.flow.common.enums.ErrorCode;
import com.flow.common.result.SakuraReply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public SakuraReply<?> handleBusinessException(BusinessException e) {
        log.error("BusinessException: code={}, message={}", e.getCode(), e.getMessage());
        return SakuraReply.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public SakuraReply<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: ", e);
        return SakuraReply.error(ErrorCode.SYSTEM_ERROR, "抓虫来!: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public SakuraReply<?> handleException(Exception e) {
        log.error("Exception: ", e);
        return SakuraReply.error(ErrorCode.SYSTEM_ERROR, "抓虫来!: " + e.getMessage());
    }
}
