package com.greedy.zupzup.global.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ApplicationException extends RuntimeException {

    private final ExceptionCode code;

    public ApplicationException(ExceptionCode code) {
        super(code.getDetail());
        this.code = code;
    }
}
