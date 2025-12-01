// package com.zx.cuicuisha.validator;
//
// import javax.validation.Constraint;
// import javax.validation.Payload;
// import java.lang.annotation.*;
//
// /**
//  * 自定义验证注解 - 验证博主ID是否存在
//  */
// @Target({ElementType.FIELD, ElementType.PARAMETER})
// @Retention(RetentionPolicy.RUNTIME)
// @Constraint(validatedBy = VlogerIdValidator.class)
// @Documented
// public @interface ValidVlogerId {
//
//    String message() default "博主ID不存在，请检查用户信息";
//
//    Class<?>[] groups() default {};
//
//    Class<? extends Payload>[] payload() default {};
// }