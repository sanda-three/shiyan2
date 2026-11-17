package com.lab.emp.common;

/**
 * 业务异常（步骤 4.3）：Service/Controller 中主动抛出的可预期错误，
 * 由 GlobalExceptionHandler 统一捕获并转换为 Result 返回，
 * 避免各层手写 try/catch 或散落的错误码
 */
public class BizException extends RuntimeException {

    /** 业务状态码，与 Result.code 对应 */
    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
