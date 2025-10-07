package com.zx.cuicuisha.grace.result;

/**
 * @Description: 自定义响应数据结构 (老版本)
 * 				本类可提供给 H5/ios/安卓/公众号/小程序 使用
 * 				前端接受此类数据（json object)后，可自行根据业务去实现相关功能
 * 				200：表示成功
 * 				500：表示错误，错误信息在msg字段中
 * 				501：bean验证错误，不管多少个错误都以map形式返回
 * 				502：拦截器拦截到用户token出错
 * 				555：异常抛出信息
 * 				556: 用户qq校验异常
 * 			   557: 校验用户是否在CAS登录，用户门票的校验
 */
public class ResponseDataStructure {

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;
    
    private String ok;	// 不使用

    public static ResponseDataStructure build(Integer status, String msg, Object data) {
        return new ResponseDataStructure(status, msg, data);
    }

    public static ResponseDataStructure build(Integer status, String msg, Object data, String ok) {
        return new ResponseDataStructure(status, msg, data, ok);
    }
    
    public static ResponseDataStructure ok(Object data) {
        return new ResponseDataStructure(data);
    }

    public static ResponseDataStructure ok() {
        return new ResponseDataStructure(null);
    }
    
    public static ResponseDataStructure errorMsg(String msg) {
        return new ResponseDataStructure(500, msg, null);
    }

    public static ResponseDataStructure errorUserTicket(String msg) {
        return new ResponseDataStructure(557, msg, null);
    }
    
    public static ResponseDataStructure errorMap(Object data) {
        return new ResponseDataStructure(501, "error", data);
    }
    
    public static ResponseDataStructure errorTokenMsg(String msg) {
        return new ResponseDataStructure(502, msg, null);
    }
    
    public static ResponseDataStructure errorException(String msg) {
        return new ResponseDataStructure(555, msg, null);
    }
    
    public static ResponseDataStructure errorUserQQ(String msg) {
        return new ResponseDataStructure(556, msg, null);
    }

    public ResponseDataStructure() {

    }

    public ResponseDataStructure(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    
    public ResponseDataStructure(Integer status, String msg, Object data, String ok) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.ok = ok;
    }

    public ResponseDataStructure(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    public Boolean isOK() {
        return this.status == 200;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

	public String getOk() {
		return ok;
	}

	public void setOk(String ok) {
		this.ok = ok;
	}

}
