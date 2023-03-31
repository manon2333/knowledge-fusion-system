package com.edu.bistu.datacollectproofaudit.utils;

/**
 * 基于Rest的知识库查询接口响应对象
 * @author ruoyuchen
 */
public class QueryResponse {

    /**
     * 工厂方法，用于快速生成访问出现错误时的响应消息
     * @param errMsg 错误消息
     * @return 返回封装好的对象
     */
    public static QueryResponse genErr(String errMsg){
        QueryResponse response = new QueryResponse();
        response.setSuccess(false);
        response.setMsg(errMsg);
        response.setJson(null);
        return response;
    }

    /**
     * 请求是否成功
     */
    private boolean success;

    //如果请求不成功，返回的错误消息。
    private String msg;

    //返回结果的JSON值
    private String json;

    private String code;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setCode(String code){this.code=code;}

    public  String getCode(){return code;}
}