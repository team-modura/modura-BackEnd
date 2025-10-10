package com.modura.modura_server.global.exception;

import com.modura.modura_server.global.response.code.BaseCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final BaseCode baseCode;

    public GlobalException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }
}