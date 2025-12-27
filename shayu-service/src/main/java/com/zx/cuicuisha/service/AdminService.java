package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.AdminLoginBO;
import com.zx.cuicuisha.pojo.AdminUsers;

public interface AdminService {
    /**
     * 管理员登录
     * @param adminLoginBO 登录信息（用户名和密码）
     * @return 管理员信息（包含token）
     */
    AdminUsers adminLogin(AdminLoginBO adminLoginBO);

    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员信息
     */
    AdminUsers queryAdminByUsername(String username);

    /**
     * 根据ID查询管理员
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    AdminUsers getAdminById(Long adminId);
}

