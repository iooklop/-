package com.resourceshare.exception;

import com.resourceshare.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public Result customError(CustomException e) {
        log.error("自定义异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据完整性异常：{}", e.getMessage());
        String message = "数据保存失败，可能是字段约束冲突";
        if (e.getMessage().contains("Duplicate entry")) {
            message = "数据已存在，无法重复添加";
        } else if (e.getMessage().contains("cannot be null")) {
            message = "必填字段不能为空";
        }
        return Result.error(message);
    }

    @ExceptionHandler(value = DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleDataAccess(DataAccessException e) {
        log.error("数据库访问异常：{}", e.getMessage(), e);
        return Result.error("数据库操作失败，请稍后重试");
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMaxUpload(MaxUploadSizeExceededException e) {
        log.error("文件过大：{}", e.getMessage());
        return Result.error("上传文件过大，请检查文件大小限制");
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("参数校验失败：{}", errors);
        return Result.error("参数校验失败", errors);
    }

    @ExceptionHandler(value = Exception.class)
    public Result error(Exception e) {
        log.error("异常信息：{}", e.getMessage(), e);
        return Result.error("服务器异常：" + e.getMessage());
    }
}
