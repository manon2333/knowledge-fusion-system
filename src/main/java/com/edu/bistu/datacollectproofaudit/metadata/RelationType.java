package com.edu.bistu.datacollectproofaudit.metadata;

import java.util.Objects;

/**
 * 关系类型元数据类
 */
public class RelationType {

    private String name; //关系名称

    private String domain; //关系头实体

    private String range; //关系尾实体

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

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationType that = (RelationType) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, domain, range);
    }

    @Override
    public String toString() {
        return "RelationType{" +
                "name='" + name + '\'' +
                ", domain='" + domain + '\'' +
                ", range='" + range + '\'' +
                '}';
    }
}
