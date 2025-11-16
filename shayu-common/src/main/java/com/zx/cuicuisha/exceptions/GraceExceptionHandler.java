package com.zx.cuicuisha.exceptions;

import com.zx.cuicuisha.grace.result.ResponseDataStructureEnum;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截处理
 * 可以针对异常的类型进行捕获，然后返回json信息到前端
 */
@ControllerAdvice       // 这个注解告诉 Spring：这个类会处理所有控制器抛出的异常
public class GraceExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public ResponseDataStructureEnum returnMyException(MyCustomException e) {
        e.printStackTrace();
        return ResponseDataStructureEnum.exception(e.getResponseStatusEnum());
    }

    // 方法中参数无法校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)    // 定义处理特定类型异常的方法
    @ResponseBody
    public ResponseDataStructureEnum returnMethodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> map = getErrors(result);
        return ResponseDataStructureEnum.errorMap(map);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseDataStructureEnum returnMaxUploadSize(MaxUploadSizeExceededException e) {
        // e.printStackTrace();
        return ResponseDataStructureEnum.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_2MB_ERROR);
    }

    public Map<String, String> getErrors(BindingResult result) {

        Map<String, String> errorMap = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();

        for (FieldError error : errorList) {
            // 错误所对应的属性字段名
            String field = error.getField();
            // 错误信息
            String defaultMessage = error.getDefaultMessage();
            errorMap.put(field, defaultMessage);
        }
        return errorMap;
    }
}
