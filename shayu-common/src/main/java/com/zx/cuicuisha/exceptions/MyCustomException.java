package com.zx.cuicuisha.exceptions;

import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义异常
 * 目的：统一处理异常信息
 *      便于解耦，拦截器、service与controller 异常错误的解耦，
 *      不会被service返回的类型而限制
 */
@Getter
@Setter
public class MyCustomException extends RuntimeException {

    private ResponseStatusEnum responseStatusEnum;

    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        super("异常状态码为：" + responseStatusEnum.status()
                + "；具体异常信息为：" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }

}
