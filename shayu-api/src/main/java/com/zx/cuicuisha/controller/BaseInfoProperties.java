package com.zx.cuicuisha.controller;

import com.zx.cuicuisha.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseInfoProperties {

   @Autowired
   public RedisOperator redis;

   public static final String MOBILE_SMSCODE = "mobile:smscode";
   public static final String REDIS_USER_TOKEN = "redis_user_token";
   public static final String REDIS_USER_INFO = "redis_user_info";


}
