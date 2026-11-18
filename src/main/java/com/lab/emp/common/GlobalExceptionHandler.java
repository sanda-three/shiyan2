package com.lab.emp.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 步骤 4.3：全局异常处理（统一返回结果封装的兜底环节）
 * @RestControllerAdvice 拦截所有 Controller 抛出的异常并转为 Result，
 * 保证任何路径（正常/业务异常/数据库约束冲突/未知异常）的响应结构一致
 */
@RestControllerAdvice(basePackages = "com.lab.emp.controller")
public class GlobalExceptionHandler {

    /** 业务异常：按抛出时指定的业务码返回 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 唯一约束/主键冲突（如绕过前置校验并发插入重复 uk_email） */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        return Result.fail(409, "数据冲突：已存在违反唯一约束的记录");
    }

    /** 参数校验失败（@Valid 未通过）：拼接各字段的中文提示返回 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(400, "参数校验失败：" + msg);
    }

    /** 请求体缺失/JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求体缺失或 JSON 格式错误");
    }

    /** 其余未预期异常：统一 500，不向前端暴露堆栈细节 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return Result.fail(500, "服务器内部错误：" + e.getClass().getSimpleName());
    }
}
