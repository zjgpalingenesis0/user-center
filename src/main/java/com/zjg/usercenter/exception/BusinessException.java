package com.zjg.usercenter.exception;

import com.zjg.usercenter.common.ErrorCode;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BusinessException(int code, String description, String message) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
