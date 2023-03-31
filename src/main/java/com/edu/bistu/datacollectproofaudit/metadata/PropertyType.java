package com.edu.bistu.datacollectproofaudit.metadata;

import java.util.List;

/**
 * 属性数据元数据类
 */
public class PropertyType {

    private String name;//属性名称

    private String domain; //属性的定义域

    private List<String> unit;//属性的值域，为属性的单位

    private DataType dataType; //属性的类型

    private Boolean allowRangeValue;//属性值是否是一个范围

    private String qualifier; //属性名称的限定符号，如 满载 排水量中，"满载"为排水量的限定符

    private String defaultUnit; //属性的默认单位

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getUnit() {
        return unit;
    }

    public void setUnit(List<String> unit) {
        this.unit = unit;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Boolean getAllowRangeValue() {
        return allowRangeValue;
    }

    public void setAllowRangeValue(Boolean allowRangeValue) {
        this.allowRangeValue = allowRangeValue;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    @Override
    public String toString() {
        return "PropertyType{" +
                "name='" + name + '\'' +
                ", domain='" + domain + '\'' +
                ", unit=" + unit +
                ", dataType=" + dataType +
                ", allowRangeValue=" + allowRangeValue +
                ", qualifier='" + qualifier + '\'' +
                ", defaultUnit='" + defaultUnit + '\'' +
                '}';
    }
}
