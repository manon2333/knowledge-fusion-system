package com.edu.bistu.datacollectproofaudit.pojo;

public class Field {
    /**
     * 字段名
     */
    private String field;
    /**
     * 字段类型
     */
    private String fieldtype;
    /**
     * update_time列
     */
    private String updatetime;

    /**
     * title列
     */
    private String title;
    /**
     *html列
     */
    private String html;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *是否是数据源列
     */
    private String isdatasource;
    /**
     *数据源名
     */
    private String datasource;


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
    public String getIsdatasource() {
        return isdatasource;
    }

    public void setIsdatasource(String isdatasource) {
        this.isdatasource = isdatasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
