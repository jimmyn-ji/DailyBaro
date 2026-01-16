package com.dailybaro.common.util;

public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 默认的成功返回
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200); // 成功的返回码
        result.setMessage("success"); // 成功的消息
        result.setData(data);
        return result;
    }

    // 默认的失败返回
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500); // 失败的返回码
        result.setMessage(message); // 失败的消息
        return result;
    }

    // 错误代码和消息的设置
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // 获取成功的返回信息
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    // getter 和 setter 方法

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}