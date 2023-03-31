package com.edu.bistu.datacollectproofaudit.controller;


import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.pojo.Verify;
import com.edu.bistu.datacollectproofaudit.service.ProofreadingService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.edu.bistu.datacollectproofaudit.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.sql.Timestamp;

/**
 * @author fuanna
 *
 */
@RestController
public class ProofreadingController {


    private final ProofreadingService proofreadingService;

    private final UserMapper userMapper;


    public ProofreadingController(@Autowired ProofreadingService proofreadingService, UserMapper userMapper){
        this.proofreadingService = proofreadingService;
        this.userMapper = userMapper;
    }


    /**
     * 查询所有校对人名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/selectProofreader",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectProofreader(){
        return proofreadingService.selectProofreader();
    }



    /**
     * 查询数据源(同步表列表页)
     * @param datasourcetype 数据源类型
     * @param id ID
     * @param keywords 关键词
     * @param proofreader 校对人
     * @param isproof 是否校对
     * @param acquisitionTimeStart 采集开始时间
     * @param acquisitionTimeEnd 采集结束时间
     * @param proofreadingTimeStart 校对开始时间
     * @param proofreadingTimeEnd 校对结束时间
     * @param proofreadingType 校对类型
     * @param pageNumber 第几页
     * @param displayNumber 展示几页
     * @param user 用户名
     * @return QueryResponse
     *
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/viewdatasource",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse viewDatasource(@RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype,//数据源类型
                                        @RequestParam(name = "id",defaultValue = "") Integer id,//ID
                                        @RequestParam(name = "keywords",defaultValue = "") String keywords,//关键字
                                        @RequestParam(name = "proofreader",defaultValue = "") String proofreader,//操作人
                                        @RequestParam(name = "isproof",defaultValue = "0") int isproof,//校对状态
                                        @RequestParam(name = "acquisitionTimeStart",defaultValue = "") String acquisitionTimeStart,//采集开始时间
                                        @RequestParam(name = "acquisitionTimeEnd",defaultValue = "") String acquisitionTimeEnd,//采集结束时间
                                        @RequestParam(name = "proofreadingTimeStart",defaultValue = "") String proofreadingTimeStart,//校对开始时间
                                        @RequestParam(name = "proofreadingTimeEnd",defaultValue = "") String proofreadingTimeEnd,//校对结束时间
                                        @RequestParam(name = "proofreadingType",defaultValue = "") String proofreadingType,//校对类型
                                        @RequestParam(name = "pageNumber",defaultValue = "1") int pageNumber,//页数
                                        @RequestParam(name = "displayNumber",defaultValue = "10") int displayNumber,//展示数量
                                        @RequestParam(name = "user",defaultValue = "") String user) {//用户名

        Timestamp acquisitionTimeStartDate=null;
        Timestamp acquisitionTimeEndDate=null;
        Timestamp proofreadingTimeStartDate=null;
        Timestamp proofreadingTimeEndDate=null;
        if(!acquisitionTimeStart.equals("")){
            acquisitionTimeStartDate=Timestamp.valueOf(acquisitionTimeStart);
        }
        if(!acquisitionTimeEnd.equals("")){
            acquisitionTimeEndDate=Timestamp.valueOf(acquisitionTimeEnd);
        }
        if(!proofreadingTimeStart.equals("")){
            proofreadingTimeStartDate=Timestamp.valueOf(proofreadingTimeStart);
        }
        if(!proofreadingTimeEnd.equals("")){
           proofreadingTimeEndDate=Timestamp.valueOf(proofreadingTimeEnd);
        }
        return proofreadingService.viewDatasource(datasourcetype,id,keywords,proofreader,isproof,acquisitionTimeStartDate,acquisitionTimeEndDate,proofreadingTimeStartDate,proofreadingTimeEndDate,pageNumber,displayNumber,user);
    }



    /**
     * 根据id删除同步表中数据
     * @param datasourcetype 数据源类型
     * @param id ID
     * @param ids id列表
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/deletedatasource",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse deleteDatasource(@RequestParam(name = "datasourcetype",defaultValue = "") int datasourcetype,//数据源类型
                                          @RequestParam(name = "id",defaultValue = "") Integer id,//ID
                                          @RequestParam(name = "ids") List<Integer> ids){//ids
        return proofreadingService.deleteDatasource(datasourcetype,id,ids);
    }


    /**
     * 添加或修改三元组
     * @param datasourceid 数据源ID（数据源ID+同步表数据ID）
     * @param tripleFile 文本
     * @param subject 主语
     * @param predicate 谓语
     * @param object 宾语
     * @param unit 单位
     * @param predicateType 谓语类型
     * @param proofReader 校对人
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/savetriplet",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse saveTriplet(@RequestParam(value = "id", defaultValue = "") Integer id,
                                     @RequestParam(value = "datasourceid", defaultValue = "") String datasourceid,
                                     @RequestParam(value = "tripleFile", defaultValue = "") String tripleFile,
                                     @RequestParam(value = "subject", defaultValue = "") String subject,
                                     @RequestParam(value = "subjectAmount", defaultValue = "") Integer subjectAmount,
                                     @RequestParam(value = "predicate", defaultValue = "") String predicate,
                                     @RequestParam(value = "object", defaultValue = "") String object,
                                     @RequestParam(value = "objectAmount", defaultValue = "") Integer objectAmount,
                                     @RequestParam(value = "unit", defaultValue = "") String unit,
                                     @RequestParam(value = "predicateType", defaultValue = "") Integer predicateType,
                                     @RequestParam(value = "proofReader", defaultValue = "") String proofReader){
                                    // @RequestParam(value = "reviewer",defaultValue = "")String reviewer,
                                     //@RequestParam(value = "rev_content",defaultValue = "")String rev_content,
                                     //@RequestParam(value = "status",defaultValue = "")Integer status){
        return proofreadingService.saveTriplet(id,datasourceid,tripleFile.trim(),subject.trim(),subjectAmount,predicate.trim(),object.trim(),objectAmount,unit,predicateType,proofReader);
    }

    /**
     * 统一保存三元组
     * @param verifyList verify列表
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/savealltriplet",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse saveAllTriplet(@RequestBody List<Verify> verifyList){
        int proof = TokenUtils.getTokenUserId();
        String proofReader= userMapper.findUsernameById(String.valueOf(proof));
        return proofreadingService.saveAllTriplet(verifyList,proofReader);
    }


    //TODO

    /**
     * 添加别名时支持搜索
     * @param entityName 实体名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/queryentityaliasname",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse queryEntityAliasName(  @RequestParam(value = "entityName", defaultValue = "") String entityName){
        return proofreadingService.queryAliasName(entityName);
    }

    /**
     * 删除三元组
     * @param id 三元组唯一id
     * @return
     */
    @RequestMapping(value = "/proofreading/deleteTriplet",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse deleteTriplet(@RequestParam(name = "id",defaultValue = "") Integer id){
        return proofreadingService.deleteTri(id);
    }



    /**
     * 校对完成、审核完成、返工完成进行的状态更改
     * @param datasourceid 数据源ID
     * @param status 数据状态 0：未标注； 1：已标注；2：已通过；3：已打回；4：已返工
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/proofreadingcompleted",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse proofreadingCompleted(@RequestParam(value = "datasourceid", defaultValue = "") String datasourceid,
                                               @RequestParam(value = "status", defaultValue = "")int status){
        return proofreadingService.proofreadingCompleted(datasourceid,status);
    }


    /**
     * 查询所有子类
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checksubcategories",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse CheckSubcategoriesByMajorCategories() {
        return proofreadingService.CheckSubcategoriesByMajorCategories();
    }

    //TODO
    /**
     * 根据实体名查询该页所标注实体
     */

    /**
     * 根据数据源ID查询该数据源页的所有实体类别
     * @param datasourceid 数据源ID
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checkEntityType",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse checkEntityType(@RequestParam(value = "datasourceid", defaultValue = "") String datasourceid) {
        return proofreadingService.checkEntityType(datasourceid);
    }


    /**
     * 查询所有实体属性的对应值
     * @param categoryId  类别ID
     * @param entityName 实体名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checkAttributeAndValue",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse checkAttributeAndValue(@RequestParam(value = "categoryId", defaultValue = "") String categoryId,
                                                @RequestParam(value = "entityName", defaultValue = "") String entityName,
                                                @RequestParam(value = "datasourceId", defaultValue = "") String datasourceId) {
        return proofreadingService.checkAttributeAndValue(categoryId,entityName,datasourceId);
    }

    /**
     * 查询某类下实体的所有关系
     * @param categoryId 类别ID
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checkrelation",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse CheckRelation(@RequestParam(value = "categoryId", defaultValue = "") String categoryId) {
        return proofreadingService.CheckRelation(categoryId);
    }


    /**
     * 查询某类下实体的所有兄弟
     * @param categoryId 类别ID
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checkBrothers",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse checkBrothers(@RequestParam(value = "categoryId", defaultValue = "") String categoryId) {
        return proofreadingService.checkBrothers(categoryId);
    }

    /**
     *查询某实体名下的关系
     * @param entityName 实体名
     * @return QueryResponse
     */
    //TODO
    // 加上页面ID
    @UserLoginToken
    @RequestMapping(value = "/proofreading/checkRelationAndValue",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse checkRelationAndValue(@RequestParam(value = "entityName", defaultValue = "") String entityName,
                                               @RequestParam(value = "datasourceId", defaultValue = "") String datasourceId) {
        return proofreadingService.checkRelationAndValue(entityName,datasourceId);
    }


    /**
     * 根据实体类型返回所有实体名
     * @param entitytype 实体类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/queryentitynamebyentitytype",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse queryEntityNameByEntityType(@RequestParam(value = "entitytype", defaultValue = "") String entitytype) {
        return proofreadingService.queryEntityNameByEntityType(entitytype);
    }



    /**
     * 选定关系名后返回所有可以选择的实体名
     * @param relation 关系
     * @param categoryId 类别ID
     * @return  QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/queryentityByRelation",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse  queryentityByRelation(@RequestParam(value = "relation", defaultValue = "") String relation,
                                         @RequestParam(value = "categoryId", defaultValue = "") String categoryId) {
        return proofreadingService.queryentityByRelation(relation,categoryId) ;
    }

    /**
     * 实体列表页进入实体详情页（获取实体的网页内容）
     * @param id ID
     * @param datasourceId 数据源名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/enterDetailsPage",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse enterDetailsPage(@RequestParam(value = "id", defaultValue = "") Integer id,//当前数据源的id
                                          @RequestParam(value = "datasourceId", defaultValue = "") Integer datasourceId){//当前实体详情页的id
        return proofreadingService.getEntityContent(id , datasourceId);
    }

    /**
     * 删除与某实体相关的所有三元组
     * @param entityName 实体名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/deleteEntity",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse deleteEntity(@RequestParam(value = "entityName", defaultValue = "") String entityName){//实体名
        return proofreadingService.deleteEntity(entityName);
    }

    /**
     * 查看实体Html详情页已存在的实体(根据实体名称和实体类型)
     * @param datasourceId 数据源ID
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/getAllEntity",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse getAllEntity(@RequestParam(value = "datasourceId", defaultValue = "") String datasourceId,
                                      @RequestParam(value = "entityType", defaultValue = "") String entityType,
                                      @RequestParam(value = "entityName", defaultValue = "") String entityName) {
        return proofreadingService.getAllEntity(datasourceId, entityType,entityName);
    }

    /**
     * 查询三元组（属性或关系列表）
     * @param entity 实体名
     * @param predicateType 谓语类型
     * @return QueryResponse
     */
//    @RequestMapping(value = "/proofreading/selecttriple",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
//    public QueryResponse selectTriple(@RequestParam(value = "entity", defaultValue = "") String entity,
//                                      @RequestParam(value = "predicateType", defaultValue = "") Integer predicateType){
//        return proofreadingService.selectTriple(entity,predicateType);
//    }

    /**
     * 保存所有属性
     * @param datasourceId 当前HTML页面的id
     * @param properties 属性名 属性值 text文本
     * @param entityName 实体名
     * @param proofReader 校对人
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/saveProperties",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse saveProperties(@RequestParam(value = "datasourceId", defaultValue = "") String datasourceId,
                                        @RequestBody List<List<String>> properties,
                                        @RequestParam(value = "entityName", defaultValue = "") String entityName,
                                        @RequestParam(value = "proofReader", defaultValue = "") String proofReader){
        return proofreadingService.saveProperties(datasourceId,properties,entityName.trim(),proofReader);
    }




    /**
     * 更改实体类型
     * @param entityType 实体类型
     * @param entityName 实体名
     * @param newEntityType 更改后的实体类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/changeEntityType",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse changeEntityType(@RequestParam(value = "entityType", defaultValue = "") String entityType,
                                          @RequestParam(value = "entityName", defaultValue = "") String entityName,
                                          @RequestParam(value = "newEntityType", defaultValue = "") String newEntityType ){
        return proofreadingService.changeEntityType(entityType, entityName, newEntityType);
    }


}

