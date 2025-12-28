package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.ReportBO;
import com.zx.cuicuisha.pojo.Report;
import com.zx.cuicuisha.utils.PagedGridResult;

public interface ReportService {
    /**
     * 举报视频
     * @param reportBO 举报信息
     * @return 举报记录
     */
    Report reportVlog(ReportBO reportBO);

    /**
     * 举报评论
     * @param reportBO 举报信息
     * @return 举报记录
     */
    Report reportComment(ReportBO reportBO);

    /**
     * 查询举报列表（管理端）
     * @param reportType 举报类型（可选，1-视频，2-评论）
     * @param status 处理状态（可选，0-待处理，1-已处理，2-已驳回）
     * @param targetId 被举报对象ID（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PagedGridResult queryReports(Integer reportType, Integer status, String targetId, Integer page, Integer pageSize);

    /**
     * 处理举报（管理端）
     * @param reportId 举报ID
     * @param handlerId 处理人ID（管理员）
     * @param status 处理状态：1-已处理，2-已驳回
     * @param handleResult 处理结果说明
     * @param notifyReporter 是否通知举报人
     * @return 处理后的举报记录
     */
    Report handleReport(String reportId, String handlerId, Integer status, String handleResult, Boolean notifyReporter);

    /**
     * 修改已处理的举报记录（同时更新SQL和MongoDB消息）
     * @param reportId 举报ID
     * @param handlerId 处理人ID（管理员）
     * @param status 处理状态：1-已处理，2-已驳回
     * @param handleResult 处理结果说明
     * @return 修改后的举报记录
     */
    Report updateReport(String reportId, String handlerId, Integer status, String handleResult);

    /**
     * 重置举报的处理记录（将状态归零，清空处理信息，并删除MongoDB消息）
     * @param reportId 举报ID
     * @return 重置后的举报记录
     */
    Report resetReport(String reportId);

    /**
     * 统计指定对象的举报次数
     * @param reportType 举报类型：1-视频，2-评论
     * @param targetId 被举报对象ID
     * @return 举报次数
     */
    Integer countReportsByTarget(Integer reportType, String targetId);

    /**
     * 统计所有举报的数量（不包括已逻辑删除的）
     * @return 所有举报的数量
     */
    Integer getTotalReportCount();

    /**
     * 统计所有视频举报的数量（不包括已逻辑删除的）
     * @return 视频举报的数量
     */
    Integer getTotalVlogReportCount();

    /**
     * 统计所有评论举报的数量（不包括已逻辑删除的）
     * @return 评论举报的数量
     */
    Integer getTotalCommentReportCount();
}

