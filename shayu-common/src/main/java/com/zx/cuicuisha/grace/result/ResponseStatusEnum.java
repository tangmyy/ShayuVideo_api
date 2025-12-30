package com.zx.cuicuisha.grace.result;

/**
 * @Description: 响应状态枚举
 * 响应结果枚举，用于提供给GraceJSONResult返回给前端的
 * 本枚举类中包含了很多的不同的状态码供使用，可以自定义
 * 便于更优雅的对状态码进行管理，一目了然
 */
public enum ResponseStatusEnum {

    SUCCESS(200, true, "操作成功！"),
    FAILED(500, false, "操作失败！"),

    // 50x
    UN_LOGIN(501,false,"请登录后再继续操作！"),
    TICKET_INVALID(5021,false,"会话失效，请重新登录！"),
    TOKEN_INVALID_ERROR(5022,false,"TOKEN参数不能为空！"),
    NO_AUTH(503,false,"您的权限不足，无法继续操作！"),
    MOBILE_ERROR(504,false,"短信发送失败，请稍后重试！"),
    SMS_NEED_WAIT_ERROR(505,false,"短信发送太快啦~请稍后再试！"),
    SMS_CODE_ERROR(506,false,"验证码过期或不匹配，请稍后再试！"),
    USER_FROZEN(507,false,"用户已被封禁，请联系管理员！"),
    USER_MUTED(5071,false,"您已被禁言，无法发布评论！"),
    USER_BANNED(5072,false,"您已被封禁，无法进行此操作！"),
    USER_UPDATE_ERROR(508,false,"用户信息更新失败，请联系管理员！"),
    USER_INACTIVE_ERROR(509,false,"请前往[账号设置]修改信息激活后再进行后续操作！"),
    USER_INFO_UPDATED_ERROR(5091,false,"用户信息修改失败！"),
    USER_INFO_UPDATED_NICKNAME_EXIST_ERROR(5092,false,"昵称已经存在！"),
    USER_INFO_UPDATED_VIDEONUM_EXIST_ERROR(5092,false,"账号已经存在！"),
    USER_INFO_CANT_UPDATED_VIDEONUM_ERROR(5092,false,"账号无法修改！"),
    FILE_UPLOAD_NULL_ERROR(510,false,"文件不能为空，请选择一个文件再上传！"),
    FILE_UPLOAD_FAILD(511,false,"文件上传失败！"),
    FILE_FORMATTER_FAILD(512,false,"文件图片格式不支持！"),
    FILE_MAX_SIZE_500KB_ERROR(5131,false,"仅支持500kb大小以下的图片上传！"),
    FILE_MAX_SIZE_2MB_ERROR(5132,false,"仅支持2MB大小以下的图片上传！"),
    FILE_NOT_EXIST_ERROR(514,false,"你所查看的文件不存在！"),
    USER_STATUS_ERROR(515,false,"用户状态参数出错！"),
    USER_NOT_EXIST_ERROR(516,false,"当前用户不存在！"),
    VLOGER_NOT_EXIST_ERROR(517,false,"被关注的用户不存在！"),
    FANS_FOLLOW_YOURSELF_ERROR(517,false,"用户不能关注自己！"),
    VLOG_NOT_EXIST_ERROR(518, false,"视频不存在"),
    VLOG_DELETED_ERROR(5181, false, "视频已被删除，无法举报！"),
    VLOG_BANNED_CANNOT_MODIFY_PRIVACY(5182, false, "视频已被封禁，无法修改公开/私密状态！"),
    COMMENT_NOT_EXIST_ERROR(520, false, "评论不存在！"),
    COMMENT_DELETED_ERROR(5201, false, "评论已被删除，无法举报！"),
    REPORT_ALREADY_EXISTS_ERROR(519, false, "您已经举报过该内容，请勿重复举报！"),
    REPORT_SELF_CONTENT_ERROR(521, false, "不能举报自己发布的内容！"),



    // 自定义系统级别异常 54x
    SYSTEM_INDEX_OUT_OF_BOUNDS(541, false, "系统错误，数组越界！"),
    SYSTEM_ARITHMETIC_BY_ZERO(542, false, "系统错误，无法除零！"),
    SYSTEM_NULL_POINTER(543, false, "系统错误，空指针！"),
    SYSTEM_NUMBER_FORMAT(544, false, "系统错误，数字转换异常！"),
    SYSTEM_PARSE(545, false, "系统错误，解析异常！"),
    SYSTEM_IO(546, false, "系统错误，IO输入输出异常！"),
    SYSTEM_FILE_NOT_FOUND(547, false, "系统错误，文件未找到！"),
    SYSTEM_CLASS_CAST(548, false, "系统错误，类型强制转换错误！"),
    SYSTEM_PARSER_ERROR(549, false, "系统错误，解析出错！"),
    SYSTEM_DATE_PARSER_ERROR(550, false, "系统错误，日期解析出错！"),

    // admin 管理系统 56x
    ADMIN_USERNAME_NULL_ERROR(561, false, "管理员登录名不能为空！"),
    ADMIN_USERNAME_EXIST_ERROR(562, false, "管理员登录名已存在！"),
    ADMIN_NAME_NULL_ERROR(563, false, "管理员负责人不能为空！"),
    ADMIN_PASSWORD_ERROR(564, false, "密码不能为空后者两次输入不一致！"),
    ADMIN_CREATE_ERROR(565, false, "添加管理员失败！"),
    ADMIN_PASSWORD_NULL_ERROR(566, false, "密码不能为空！"),
    ADMIN_NOT_EXIT_ERROR(567, false, "管理员不存在或密码错误！"),
    ADMIN_FACE_NULL_ERROR(568, false, "人脸信息不能为空！"),
    ADMIN_FACE_LOGIN_ERROR(569, false, "人脸识别失败，请重试！"),
    CATEGORY_EXIST_ERROR(570, false, "文章分类已存在，请换一个分类名！"),
    REPORT_NOT_EXIST_ERROR(571, false, "举报记录不存在！"),
    REPORT_DELETED_ERROR(572, false, "举报记录已被删除！"),
    REPORT_STATUS_INVALID_ERROR(573, false, "举报状态参数错误，状态必须是1（已处理）或2（已驳回）！"),
    REPORT_NOT_HANDLED_ERROR(574, false, "只能修改已处理的举报记录，当前记录尚未处理！"),

    // 媒体中心 相关错误 58x
    ARTICLE_COVER_NOT_EXIST_ERROR(580, false, "文章封面不存在，请选择一个！"),
    ARTICLE_CATEGORY_NOT_EXIST_ERROR(581, false, "请选择正确的文章领域！"),
    ARTICLE_CREATE_ERROR(582, false, "创建文章失败，请重试或联系管理员！"),
    ARTICLE_QUERY_PARAMS_ERROR(583, false, "文章列表查询参数错误！"),
    ARTICLE_DELETE_ERROR(584, false, "文章删除失败！"),
    ARTICLE_WITHDRAW_ERROR(585, false, "文章撤回失败！"),
    ARTICLE_REVIEW_ERROR(585, false, "文章审核出错！"),
    ARTICLE_ALREADY_READ_ERROR(586, false, "文章重复阅读！"),

    // 人脸识别错误代码
    FACE_VERIFY_TYPE_ERROR(600, false, "人脸比对验证类型不正确！"),
    FACE_VERIFY_LOGIN_ERROR(601, false, "人脸登录失败！"),

    // 系统错误，未预期的错误 555
    SYSTEM_ERROR(555, false, "系统繁忙，请稍后再试！"),
    SYSTEM_OPERATION_ERROR(556, false, "操作失败，请重试或联系管理员"),
    SYSTEM_RESPONSE_NO_INFO(557, false, ""),
    SYSTEM_ERROR_GLOBAL(558, false, "全局降级：系统繁忙，请稍后再试！"),
    SYSTEM_ERROR_FEIGN(559, false, "客户端Feign降级：系统繁忙，请稍后再试！"),
    SYSTEM_ERROR_ZUUL(560, false, "请求系统过于繁忙，请稍后再试！");


    // 响应业务状态
    private Integer status;
    // 调用是否成功
    private Boolean success;
    // 响应消息，可以为成功或者失败的消息
    private String msg;

    ResponseStatusEnum(Integer status, Boolean success, String msg) {
        this.status = status;
        this.success = success;
        this.msg = msg;
    }

    public Integer status() {
        return status;
    }
    public Boolean success() {
        return success;
    }
    public String msg() {
        return msg;
    }
}
