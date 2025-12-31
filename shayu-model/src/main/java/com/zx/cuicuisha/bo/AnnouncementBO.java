package com.zx.cuicuisha.bo;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementBO {

    @NotBlank(message = "管理员ID不能为空")
    private String handlerId;

    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    @Length(max = 1000, message = "消息内容长度不能超过1000")
    private Map<String, Object> msgContent;
}

