// package com.zx.cuicuisha.validator;
//
// import com.zx.cuicuisha.service.UserService;
// import org.springframework.beans.factory.annotation.Autowired;
//
// import javax.validation.ConstraintValidator;
// import javax.validation.ConstraintValidatorContext;
//
// /**
//  * 博主ID验证器实现
//  */
// public class VlogerIdValidator implements ConstraintValidator<ValidVlogerId, String> {
//
//    // 注意：这里需要注入UserService，但由于在common模块中，我们需要特殊处理
//    private UserService userService;
//
//    /**
//     * 由于common模块不直接依赖service，我们可以通过以下方式之一解决：
//     * 1. 使用@Autowired(required = false)并检查null
//     * 2. 在应用启动时手动设置
//     * 3. 使用反射或工具类获取Service
//     */
//    @Autowired(required = false)
//    public void setUserService(UserService userService) {
//       this.userService = userService;
//    }
//
//    @Override
//    public void initialize(ValidVlogerId constraintAnnotation) {
//       // 初始化操作
//    }
//
//    @Override
//    public boolean isValid(String vlogerId, ConstraintValidatorContext context) {
//       // 如果userService为null（在测试环境或某些情况下），跳过验证
//       if (userService == null) {
//          return true;
//       }
//
//       if (vlogerId == null || vlogerId.trim().isEmpty()) {
//          return false;
//       }
//
//       try {
//          // 调用UserService验证博主ID是否存在
//          return userService.isVlogerExist(vlogerId);
//       } catch (Exception e) {
//          // 记录日志，但在验证失败时返回false
//          return false;
//       }
//    }
// }