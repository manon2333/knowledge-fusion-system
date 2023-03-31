package com.edu.bistu.datacollectproofaudit.controller;

import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.pojo.Field;
import com.edu.bistu.datacollectproofaudit.pojo.UnstructureData;
import com.edu.bistu.datacollectproofaudit.service.DataSourceFactory;
import com.edu.bistu.datacollectproofaudit.service.DatasourceService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author fuanna
 *
 */
@RestController
public class DatasourceController {

    private final DatasourceService datasourceService;

    public DatasourceController(@Autowired DatasourceService datasourceService){
        this.datasourceService = datasourceService;
    }

    /**
     * 测试数据库连接
     * @param host host
     * @param port port
     * @param username 用户名
     * @param password 密码
     * @param dbtype 数据库类型
     * @param dbname 数据库名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/testconnection",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse testConnection(@RequestParam(name = "host") String host,//host
                                        @RequestParam(name = "port") String port,//port
                                        @RequestParam(name = "username") String username,//用户名
                                        @RequestParam(name = "password") String password,//密码
                                        @RequestParam(name = "dbtype") String dbtype,//数据库类型
                                        @RequestParam(name = "dbname") String dbname){//数据库名
        //dbtype字符串转枚举
        DataSourceFactory.DBType dbType = DataSourceFactory.DBType.valueOf(dbtype);
        return datasourceService.queryAllTable(host,port,username,password,dbType,dbname);
    }


    /**
     * 插入非结构化数据源信息
     * @param dataSourceName 数据源名
     * @param synchronizeCycle 同步周期
     * @param host host
     * @param port port
     * @param username 用户名
     * @param password 密码
     * @param dbtype 数据库类型
     * @param dbname 数据库名
     * @param tablename 表名
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/add",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse unstrudatasourceAdd(@RequestParam(name = "dataSourceName") String dataSourceName,//数据源名称
                                             @RequestParam(name = "synchronizeCycle") Integer synchronizeCycle,//同步周期
                                             @RequestParam(name = "host") String host,//host
                                             @RequestParam(name = "port") Integer port,//port
                                             @RequestParam(name = "username") String username,//用户名
                                             @RequestParam(name = "password") String password,//密码
                                             @RequestParam(name = "dbtype") String dbtype,//数据库类型
                                             @RequestParam(name = "dbname") String dbname,//数据库名
                                             @RequestParam(name = "tablename") String tablename){
        DataSourceFactory.DBType dbType = DataSourceFactory.DBType.valueOf(dbtype);
        UnstructureData unstructureData=new UnstructureData();
        unstructureData.setDataSourceName(dataSourceName);
        unstructureData.setSynchronizeCycle(synchronizeCycle);
        unstructureData.setHost(host);
        unstructureData.setPort(port);
        unstructureData.setUsername(username);
        unstructureData.setPassword(password);
        unstructureData.setDbtype(dbType);
        unstructureData.setDbname(dbname);
        unstructureData.setTablename(tablename);
        return datasourceService.insertUnstructureDataSource(unstructureData);
    }


    /**
     * 新建同步表
     * @param id ID
     * @param fieldlist Field列表
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/createSynchronizeTable",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse createSynchronizeTable( @RequestParam(name = "id",defaultValue = "") Integer id,
                                      @RequestBody List<Field> fieldlist){
        return datasourceService.createNewSynchronizeTable(id,fieldlist);
    }


    /**
     * 同步表中的列
     * @param id ID
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/synchronizeField",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse synchronize( @RequestParam(name = "id",defaultValue = "") Integer id){
        return datasourceService.synchronizeField(id);
    }


    /**
     * 开始自动同步
     * @param id 数据源id
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/startAutoSync",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse startAutoSync( @RequestParam(name = "id",defaultValue = "") Integer id) throws SchedulerException{
        return datasourceService.startAutoSync(id);
    }


    /**
     * 暂停自动同步
     * @param id 数据源id
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/stopAutoSync",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse stopAutoSync( @RequestParam(name = "id",defaultValue = "") Integer id) throws SchedulerException{
        return datasourceService.stopAutoSync(id);
    }


    /**
     * 查询数据源(数据源列表页)
     * @param datasourcetype 数据源类型 (0是结构化数据,1是非结构化数据,2是结构化和非结构化数据)
     * @param synchronizeState 同步周期
     * @param isdelete 是否删除 （0是未删除，1是已删除）
     * @param dataSourceName 数据源名
     * @param pageNumber 第几页
     * @param displayNumber 展示页数
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/alldatasource",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectDatasource( @RequestParam(name = "datasourcetype",defaultValue = "2") String datasourcetype,
                                           @RequestParam(name = "synchronizeState",defaultValue = "") String synchronizeState,
                                           @RequestParam(name = "isdelete",defaultValue = "") String isdelete,
                                           @RequestParam(name = "dataSourceName",defaultValue = "") String dataSourceName,
                                           @RequestParam(name = "pageNumber",defaultValue = "") int pageNumber,
                                           @RequestParam(name = "displayNumber",defaultValue = "") int displayNumber) throws JsonProcessingException {
        return datasourceService.selectDatasource(datasourcetype,synchronizeState,isdelete,dataSourceName,pageNumber,displayNumber);
    }


    /**
     * 根据ID删除数据源
     * @param id ID
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/deleteDatasourceById",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse deleteDatasourceById( @RequestParam(name = "id",defaultValue = "") Integer id,
                                               @RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype){
        return datasourceService.deleteDatasourceById(id,datasourcetype);
    }


    /**
     * 根据ID查询数据源
     * @param id ID
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/selectDatasourceById",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectDatasourceById( @RequestParam(name = "id",defaultValue = "") Integer id,
                                               @RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype){
        return datasourceService.selectDatasourceById(id,datasourcetype);
    }


    /**
     * 根据ID编辑数据源
     * @param id ID
     * @param dataSourceName 数据源名
     * @param synchronizeCycle 同步周期
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/editorDatasourceById",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse editorDatasourceById( @RequestParam(name = "id",defaultValue = "") Integer id,
                                               @RequestParam(name = "dataSourceName",defaultValue = "") String dataSourceName,
                                               @RequestParam(name = "synchronizeCycle",defaultValue = "") Integer synchronizeCycle,
                                               @RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype){
        return datasourceService.editorsDatasourceById(id,dataSourceName,synchronizeCycle,datasourcetype);
    }


    /**
     * 查看同步设置
     * @param id id
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datasource/viewsyncsettings",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse viewSyncSettings( @RequestParam(name = "id",defaultValue = "") Integer id,
                                               @RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype){
        return datasourceService.viewSyncSettings(id,datasourcetype);
    }



    /**
     * 查询数据源名
     * @return QueryResponse 返回ID，数据源名，数据源类型的list
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/datasourceName",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse datasourceName(){
        return datasourceService.selectDatasourceName();
    }

}
