package com.zx.cuicuisha.enums;

import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;

/**
 * @Desc: 消息类型
 */
public enum MessageEnum {
    FOLLOW_YOU(1, "关注", "follow"),
    LIKE_VLOG(2, "点赞视频", "likeVideo"),
    COMMENT_VLOG(3, "评论视频", "comment"),
    REPLY_YOU(4, "回复评论", "replay"),
    LIKE_COMMENT(5, "点赞评论", "likeComment"),
    REPORT_HANDLED(6, "举报处理", "reportHandled");

    public final Integer type;
    public final String value;
    public final String enValue;

    MessageEnum(Integer type, String value, String enValue) {
        this.type = type;
        this.value = value;
        this.enValue = enValue;
    }

    /**
     * 根据路由键获取对应的消息枚举
     * 路由键格式：sys.msg.{enValue}，例如：sys.msg.follow
     * @param routingKey 路由键
     * @return 对应的消息枚举，如果未找到则返回null
     */
    public static MessageEnum getByRoutingKey(String routingKey) {
        if (StringUtils.isBlank(routingKey)) {
            return null;
        }
        // 移除路由键前缀 "sys.msg."，获取enValue
        String prefix = "sys.msg.";
        if (!routingKey.startsWith(prefix)) {
            return null;
        }
        String enValue = routingKey.substring(prefix.length());
        // 遍历枚举，查找匹配的enValue
        return Arrays.stream(MessageEnum.values())
                .filter(enumItem -> enumItem.enValue.equalsIgnoreCase(enValue))
                .findFirst()
                .orElse(null);
    }
}
