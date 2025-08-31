package com.greedy.zupzup.global.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ApplicationException extends RuntimeException {

    private final ExceptionCode code;
    private final String detailOverride;

    public ApplicationException(ExceptionCode code) {
        super(code.getDetail());
        this.code = code;
        this.detailOverride = null;
    }

    public ApplicationException(ExceptionCode code, String detailOverride) {
        super(detailOverride != null ? detailOverride : code.getDetail());
        this.code = code;
        this.detailOverride = detailOverride;
    }

    public ApplicationException(ExceptionCode code, List<String> errorFields) {
        super(String.join(", ", errorFields) + " " + code.getDetail());
        this.code = code;
        this.detailOverride = String.join(", ", errorFields) + " " + code.getDetail();
    }
}
