package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.AppealAdminVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AppealMapperCustom {

    /**
     * 查询申诉列表（管理端使用）
     * @param map 查询参数，可包含appealType（申诉类型）、status（处理状态）、targetId（被申诉对象ID）、reportId（举报ID）等
     * @return 申诉列表
     */
    public List<AppealAdminVO> getAppealList(@Param("paramMap") Map<String, Object> map);

    /**
     * 检查用户是否已申诉过该举报
     * @param appealerId 申诉人ID
     * @param reportId 举报记录ID
     * @return 申诉记录ID，如果不存在则返回null
     */
    public String checkAppealExists(@Param("appealerId") String appealerId, 
                                    @Param("reportId") String reportId);

    /**
     * 重置申诉记录的处理信息（将状态归零，清空处理信息）
     * @param appealId 申诉ID
     * @return 更新的记录数
     */
    public int resetAppealStatus(@Param("appealId") String appealId);
}

