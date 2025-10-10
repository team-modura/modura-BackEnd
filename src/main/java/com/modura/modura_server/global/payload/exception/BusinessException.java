package com.modura.modura_server.global.payload.exception;

import com.modura.modura_server.global.payload.code.BaseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final BaseCode baseCode;

    public BusinessException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }
}