package com.zx.cuicuisha.vo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 管理员视图对象
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminVO {
    private Long id;                    // 管理员ID
    private String username;            // 用户名
    private String email;                // 邮箱
    private String realName;            // 真实姓名
    private String avatar;              // 头像URL
    private String adminToken;          // 管理员会话token
}

