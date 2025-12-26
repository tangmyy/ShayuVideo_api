package com.zx.cuicuisha.impl;

import com.zx.cuicuisha.base.BaseInfoProperties;
import com.zx.cuicuisha.bo.AdminLoginBO;
import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;
import com.zx.cuicuisha.mapper.AdminUsersMapper;
import com.zx.cuicuisha.pojo.AdminUsers;
import com.zx.cuicuisha.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class AdminServiceImpl extends BaseInfoProperties implements AdminService {

    @Autowired
    private AdminUsersMapper adminUsersMapper;

    /**
     * MD5加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    private String md5Encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            throw new RuntimeException("密码加密失败");
        }
    }

    // 管理员登录
    @Override
    public AdminUsers adminLogin(AdminLoginBO adminLoginBO) {
        // 1. 验证用户名和密码不能为空
        if (StringUtils.isBlank(adminLoginBO.getUsername()) || StringUtils.isBlank(adminLoginBO.getPassword())) {
            GraceException.display(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
        }
        // 2. 根据用户名查询管理员
        AdminUsers admin = queryAdminByUsername(adminLoginBO.getUsername());
        if (admin == null) {
            GraceException.display(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
        // 3. 验证密码（使用MD5加密后比对）
        String encryptedPassword = md5Encrypt(adminLoginBO.getPassword());
        if (!encryptedPassword.equals(admin.getPassword())) {
            GraceException.display(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
        // 4. 登录成功，生成token并保存到Redis
        String adminToken = java.util.UUID.randomUUID().toString();
        redis.set(REDIS_ADMIN_TOKEN + ":" + admin.getId(), adminToken);
        // 5. 清除密码字段，确保安全
        admin.setPassword(null);
        log.info("管理员登录成功：username={}, adminId={}", adminLoginBO.getUsername(), admin.getId());
        return admin;
    }

    // 根据用户名查询管理员
    @Override
    public AdminUsers queryAdminByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        Example example = new Example(AdminUsers.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        AdminUsers admin = adminUsersMapper.selectOneByExample(example);
        return admin;
    }

    // 根据ID查询管理员
    @Override
    public AdminUsers getAdminById(Long adminId) {
        if (adminId == null) {
            return null;
        }
        return adminUsersMapper.selectByPrimaryKey(adminId);
    }
}

