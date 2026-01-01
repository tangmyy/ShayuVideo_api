package com.zx.cuicuisha.service;

import com.zx.cuicuisha.bo.AppealBO;
import com.zx.cuicuisha.pojo.Appeal;
import com.zx.cuicuisha.utils.PagedGridResult;

public interface AppealService {
    /**
     * 创建申诉
     * @param appealBO 申诉信息
     * @return 申诉记录
     */
    Appeal createAppeal(AppealBO appealBO);

    /**
     * 查询申诉列表（管理端）
     * @param appealType 申诉类型（可选，1-视频，2-评论）
     * @param status 处理状态（可选，0-待处理，1-已处理（通过），2-已驳回）
     * @param targetId 被申诉对象ID（可选）
     * @param reportId 举报记录ID（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PagedGridResult queryAppeals(Integer appealType, Integer status, String targetId, String reportId, Integer page, Integer pageSize);

    /**
     * 处理申诉（管理端）
     * @param appealId 申诉ID
     * @param handlerId 处理人ID（管理员）
     * @param status 处理状态：1-已处理（通过），2-已驳回
     * @param handleResult 处理结果说明
     * @param notifyAppealer 是否通知申诉人
     * @return 处理后的申诉记录
     */
    Appeal handleAppeal(String appealId, String handlerId, Integer status, String handleResult, Boolean notifyAppealer);

    /**
     * 修改已处理的申诉记录（同时更新SQL和MongoDB消息）
     * @param appealId 申诉ID
     * @param handlerId 处理人ID（管理员）
     * @param status 处理状态：1-已处理（通过），2-已驳回
     * @param handleResult 处理结果说明
     * @return 修改后的申诉记录
     */
    Appeal updateAppeal(String appealId, String handlerId, Integer status, String handleResult);

    /**
     * 重置申诉的处理记录（将状态归零，清空处理信息，并删除MongoDB消息）
     * @param appealId 申诉ID
     * @return 重置后的申诉记录
     */
    Appeal resetAppeal(String appealId);

    /**
     * 统计所有申诉的数量（不包括已逻辑删除的）
     * @return 所有申诉的数量
     */
    Integer getTotalAppealCount();

    /**
     * 统计所有视频申诉的数量（不包括已逻辑删除的）
     * @return 视频申诉的数量
     */
    Integer getTotalVlogAppealCount();

    /**
     * 统计所有评论申诉的数量（不包括已逻辑删除的）
     * @return 评论申诉的数量
     */
    Integer getTotalCommentAppealCount();
}

