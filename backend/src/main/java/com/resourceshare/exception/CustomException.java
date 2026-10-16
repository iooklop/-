package com.resourceshare.exception;

public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String code;
    private final String msg;

    public CustomException(String msg) {
        super(msg);
        this.msg = msg;
        this.code = "500";
    }

    public CustomException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
