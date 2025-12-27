package com.zx.cuicuisha.mapper;

import com.zx.cuicuisha.vo.ReportAdminVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ReportMapperCustom {

    /**
     * 查询举报列表（管理端使用）
     * @param map 查询参数，可包含reportType（举报类型）、status（处理状态）、targetId（被举报对象ID）等
     * @return 举报列表
     */
    public List<ReportAdminVO> getReportList(@Param("paramMap") Map<String, Object> map);

    /**
     * 统计指定对象的举报次数
     * @param reportType 举报类型：1-视频，2-评论
     * @param targetId 被举报对象ID
     * @return 举报次数
     */
    public Integer countReportsByTarget(@Param("reportType") Integer reportType, @Param("targetId") String targetId);

    /**
     * 检查用户是否已举报过该对象
     * @param reporterId 举报人ID
     * @param reportType 举报类型：1-视频，2-评论
     * @param targetId 被举报对象ID
     * @return 举报记录ID，如果不存在则返回null
     */
    public String checkReportExists(@Param("reporterId") String reporterId, 
                                    @Param("reportType") Integer reportType, 
                                    @Param("targetId") String targetId);

    /**
     * 重置举报记录的处理信息（将状态归零，清空处理信息）
     * @param reportId 举报ID
     * @return 更新的记录数
     */
    public int resetReportStatus(@Param("reportId") String reportId);
}

