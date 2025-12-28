package com.zx.cuicuisha.enums;

import com.zx.cuicuisha.exceptions.GraceException;
import com.zx.cuicuisha.grace.result.ResponseStatusEnum;

/**
 * @Desc: 修改用户信息类型 枚举
 */
public enum UserInfoModifyTypeEnum {
    NICKNAME(1, "昵称"),
    VIDEONUM(2, "视频号"),
    SEX(3, "性别"),
    BIRTHDAY(4, "生日"),
    LOCATION(5, "所在地"),
    DESC(6, "简介");

    public final Integer type;
    public final String value;

    UserInfoModifyTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public static void checkUserInfoTypeIsRight(Integer type) {
        if (type != UserInfoModifyTypeEnum.NICKNAME.type &&
                type != UserInfoModifyTypeEnum.VIDEONUM.type &&
                type != UserInfoModifyTypeEnum.SEX.type &&
                type != UserInfoModifyTypeEnum.BIRTHDAY.type &&
                type != UserInfoModifyTypeEnum.LOCATION.type &&
                type != UserInfoModifyTypeEnum.DESC.type) {
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
    }
}
