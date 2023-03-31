package com.edu.bistu.datacollectproofaudit.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.Id;
import java.util.Date;

public class Verify {
    /**
     * 唯一标识
     */
//    @Id
    private Integer id;

    /**
     * 源文本id
     */
    private String datasourceid;

    /**
     * 三元组所在文本
     */
    private String tripleFile;

    /**
     * 头实体
     */
    private String subject;

    /**
     * 头实体数量
     */
    private Integer subjectAmount;

    /**
     * 类型、关系、属性
     */
    private String predicate;

    /**
     * 尾实体
     */
    private String object;

    /**
     * 尾实体数量
     */
    private Integer objectAmount;


    /**
     * 单位
     */
    private String unit;

    /**
     * 来源
     */
    private String data_source;

    /**
     * 来源的url
     */
    private String url;



    /**
     * 类型标识：
0：实体类型
1：属性
2：关系
     */
    private Integer predicateType;

    /**
     * 校对人
     */
    private String proofReader;

    /**
     * 校对时间
     */
    private Date proofTime;

    /**
     * 审核人
     */
    private String reviewer;

    /**
     * 审核批注
     */
    private String rev_content;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 状态表标识符：
     */
    private String status;

    /**
     * 是否已经入库：
0：未入库
1：已入库
     */
    private Integer intoKg;

    /**
     * 获取唯一标识（源数据库贵不行id/源文档id+性质[HTML、XML、word......]）
     *
     * @return id - 唯一标识（源数据库贵不行id/源文档id+性质[HTML、XML、word......]）
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置唯一标识（源数据库贵不行id/源文档id+性质[HTML、XML、word......]）
     *
     * @param id 唯一标识（源数据库贵不行id/源文档id+性质[HTML、XML、word......]）
     */
    public void setId(Integer id) {
        this.id = id;
    }


    /**
     * 源数据id
     *
     * @return datasourceid - 源数据id
     */
    public String getDatasourceid() {
        return datasourceid;
    }

    /**
     * 源数据id
     *
     * @param datasourceid 源数据id
     */
    public void setDatasourceid(String datasourceid) {
        this.datasourceid = datasourceid;
    }

    /**
     * 获取三元组所在文本
     *
     * @return triple_file - 三元组所在文本
     */
    public String getTripleFile() {
        return tripleFile;
    }

    /**
     * 设置三元组所在文本
     *
     * @param tripleFile 三元组所在文本
     */
    public void setTripleFile(String tripleFile) {
        this.tripleFile = tripleFile;
    }

    /**
     * 获取头实体
     *
     * @return subject - 头实体
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置头实体
     *
     * @param subject 头实体
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 获取头实体数量
     *
     * @return subjectAmount - 头实体数量
     */
    public Integer getSubjectAmount() {
        return subjectAmount;
    }

    /**
     * 设置头实体数量
     *
     * @param subjectAmount 头实体数量
     */
    public void setSubjectAmount(Integer subjectAmount) {
        this.subjectAmount = subjectAmount;
    }


    /**
     * 获取类型、关系、属性
     *
     * @return predicate - 类型、关系、属性
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * 设置类型、关系、属性
     *
     * @param predicate 类型、关系、属性
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * 获取尾实体
     *
     * @return object - 尾实体
     */
    public String getObject() {
        return object;
    }

    /**
     * 设置尾实体
     *
     * @param object 尾实体
     */
    public void setObject(String object) {
        this.object = object;
    }

    /**
     * 获取审核批注
     *
     * @return rev_content 审核批注
     */
    public String getRev_content() {
        return rev_content;
    }

    /**
     * 设置审核批注
     *
     * @param rev_content 审核批注
     */
    public void setRev_content(String rev_content) {
        this.rev_content = rev_content;
    }
    /**
     * 获取尾实体数量
     *
     * @return objectAmount - 尾实体数量
     */
    public Integer getObjectAmount() {
        return objectAmount;
    }

    /**
     * 设置尾实体数量
     *
     * @param objectAmount 尾实体数量
     */
    public void setObjectAmount(Integer objectAmount) {
        this.objectAmount = objectAmount;
    }

    /**
     * 获取单位
     *
     * @return unit - 单位
     */
    public String getUnit() {
        return unit;
    }
    /**
     * 设置单位
     *
     * @param unit 单位
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 获取来源
     * @return data_source 来源
     */
    public String getData_source() {
        return data_source;
    }

    /**
     * 设置来源
     * @param data_source
     */
    public void setData_source(String data_source) {
        this.data_source = data_source;
    }

    /**
     * 获取url
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置url
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取类型标识：
0：实体类型
1：属性
2：关系
     *
     * @return predicate_type - 类型标识：
0：实体类型
1：属性
2：关系
     */
    public Integer getPredicateType() {
        return predicateType;
    }

    /**
     * 设置类型标识：
0：实体类型
1：属性
2：关系
     *
     * @param predicateType 类型标识：
0：实体类型
1：属性
2：关系
     */
    public void setPredicateType(Integer predicateType) {
        this.predicateType = predicateType;
    }

    /**
     * 获取校对人
     *
     * @return proof_reader - 校对人
     */
    public String getProofReader() {
        return proofReader;
    }

    /**
     * 设置校对人
     *
     * @param proofReader 校对人
     */
    public void setProofReader(String proofReader) {
        this.proofReader = proofReader;
    }

    /**
     * 获取校对时间
     *
     * @return proof_time - 校对时间
     */
    public Date getProofTime() {
        return proofTime;
    }

    /**
     * 设置校对时间
     *
     * @param proofTime 校对时间
     */
    public void setProofTime(Date proofTime) {
        this.proofTime = proofTime;
    }

    /**
     * 获取审核人
     *
     * @return reviewer - 审核人
     */
    public String getReviewer() {
        return reviewer;
    }

    /**
     * 设置审核人
     *
     * @param reviewer 审核人
     */
    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * 获取审核时间
     *
     * @return review_time - 审核时间
     */
    public Date getReviewTime() {
        return reviewTime;
    }

    /**
     * 设置审核时间
     *
     * @param reviewTime 审核时间
     */
    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    /**
     * @return status 状态标识符，1是已标注，2是已审核，3是审核不通过：
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status 状态标识符，1是已标注，2是已审核，3是审核不通过：
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取是否已经入库：
0：未入库
1：已入库
     *
     * @return into_kg - 是否已经入库：
0：未入库
1：已入库
     */
    public Integer getIntoKg() {
        return intoKg;
    }

    /**
     * 设置是否已经入库：
0：未入库
1：已入库
     *
     * @param intoKg 是否已经入库：
0：未入库
1：已入库
     */
    public void setIntoKg(Integer intoKg) {
        this.intoKg = intoKg;
    }

    public Verify(){

    }




    public Verify(String datasourceid,String tripleFile, String subject, String predicate, String object,
                  Integer predicateType, String proofReader){
        this.datasourceid=datasourceid;
        this.tripleFile = tripleFile;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.predicateType = predicateType;
        this.proofReader = proofReader;
    }

}