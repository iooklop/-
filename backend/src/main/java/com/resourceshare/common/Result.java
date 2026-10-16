package com.resourceshare.common;

import lombok.Data;

@Data
public class Result {

    private static final String SUCCESS = "200";
    private static final String FAIL = "500";
    private String code;
    private String msg;
    private Object data;

    public static Result success() {
        Result result = new Result();
        result.setCode(SUCCESS);
        result.setMsg("成功");
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(SUCCESS);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(FAIL);
        result.setMsg(msg);
        return result;
    }

    public static Result error(String code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static Result error(String msg, Object data) {
        Result result = new Result();
        result.setCode(FAIL);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
