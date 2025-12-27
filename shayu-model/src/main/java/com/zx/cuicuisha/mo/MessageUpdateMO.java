package com.zx.cuicuisha.mo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 消息更新BO
 * 用于管理员更新消息信息
 * 所有字段都是可选的（除了id），只更新传入的非空字段
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document("message")
public class MessageUpdateMO {

   @Id
   @NotBlank(message = "消息ID不能为空")
   private String id;
   // 发送者用户ID（可选）
   private String fromUserId;
   // 发送者昵称（可选）
   private String fromNickname;
   // 发送者头像（可选）
   private String fromFace;
   // 接收者用户ID（可选）
   private String toUserId;
   // 消息类型（可选）
   private Integer msgType;
   /**
    * 消息内容（可选）
    * 如果传入此字段，将完全替换原有的msgContent
    * 如果需要合并更新，请在Service层实现合并逻辑
    */
   private Map<String, Object> msgContent;
   /**
    * 是否合并msgContent（可选，默认false）
    * true: 将新内容合并到原有内容中
    * false: 完全替换原有内容
    */
   private Boolean mergeContent = false;
}

