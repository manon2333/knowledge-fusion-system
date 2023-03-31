package com.edu.bistu.datacollectproofaudit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.mapper.StructureDataMapper;
import com.edu.bistu.datacollectproofaudit.mapper.UnstructureDataMapper;
import com.edu.bistu.datacollectproofaudit.pojo.Field;
import com.edu.bistu.datacollectproofaudit.pojo.StructureData;
import com.edu.bistu.datacollectproofaudit.pojo.UnstructureData;
import com.edu.bistu.datacollectproofaudit.utils.GetRandomChar;
import com.edu.bistu.datacollectproofaudit.utils.QuartzManager;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.sql.DataSource;
import java.sql.*;
import java.util.*;


@Service
public class DatasourceService{

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    private final UnstructureDataMapper unstructureDataMapper;

    private final StructureDataMapper structureDataMapper;

    private final QuartzManager quartzManager;

    public DatasourceService(@Autowired UnstructureDataMapper unstructureDataMapper,
                             @Autowired StructureDataMapper structureDataMapper,
                             @Autowired QuartzManager quartzManager){
        this.unstructureDataMapper = unstructureDataMapper;
        this.structureDataMapper = structureDataMapper;
        this.quartzManager = quartzManager;
    }


    /**
     *  @Description 根据数据库名返回该数据库下的所有表名
     * @param host hsot
     * @param port port
     * @param username 用户名
     * @param password 密码
     * @param dbtype 数据库类型
     * @param dbname 数据库名
     * @return QueryResponse
     */
    public QueryResponse queryAllTable(String host, String port, String username, String password, DataSourceFactory.DBType dbtype, String dbname){
        List<String> tableList=new ArrayList<>();
        DataSource ds = DataSourceFactory.getDataSource(dbtype, host, port, dbname, username, password);
        if(ds==null){
            return QueryResponse.genErr("无法获取数据库链接");
        }
        Connection conn = null;
        try{
            conn = ds.getConnection();
            ResultSet rs=  conn.getMetaData().getTables(conn.getCatalog(), username, null, new String[]{"TABLE"});
            while(rs.next()) {
                tableList.add(rs.getString("TABLE_NAME"));//所有表名
            }
            log.info("数据库连接成功");
        }catch(SQLException e){
            e.printStackTrace();
            log.info("数据库连接错误,错误信息：",e);
        }finally{
            if(conn!=null){
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tableList", tableList);
        if(!tableList.isEmpty()){
            response.setSuccess(true);
            response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
            log.info("根据数据库名查询表名成功！");
            response.setMsg("测试连接成功！");
        }else {
            log.info("根据数据库名查询表名失败！");
            response.setMsg("测试连接失败！");
        }
        return response;
    }

    /**
     * @Description 插入非结构化数据源信息
     * @param unstructureData 非结构化数据实体类
     * @return  QueryResponse
     */
    public QueryResponse insertUnstructureDataSource(UnstructureData unstructureData){
        String dbname = unstructureData.getDbname();
        String tablename = unstructureData.getTablename();
        String dataSourceName=unstructureData.getDataSourceName();
        // 查看非结构化数据源表中是否已存在该表（根据数据库名+表名+数据源名可以为唯一确定一条数据源）
        UnstructureData unstructure=unstructureDataMapper.selectUnstructDatasourceByDbnameAndTablename(dataSourceName,dbname,tablename);
        if(unstructure!=null){
            log.info("该数据源已存在!");
            return QueryResponse.genErr("该数据源数已存在!");
        }
        QueryResponse response = new QueryResponse();
        Timestamp date=Timestamp.valueOf("2000-1-1 0:0:0");//得到2000-1-1 0:0:0时间戳，为了在同步数据的时候插入这个时间段以后的数据，这样第一次同步和以后的同步可以是同一个操作
        unstructureData.setSynchronizePosition(date);
        Connection conn;
        int isOk = unstructureDataMapper.insertUnstructureData(unstructureData);//将同步时间数据插入非结构化数据源
        if (isOk == 0) {
            response.setMsg("插入非结构化数据源数据失败！");
            log.info("插入非结构化数据源数据失败！");
        } else {
            Map<String, String> allfieldMap=new HashMap<>();
            try {
                DataSource ds=DataSourceFactory.getDataSource(unstructureData.getDbtype(),
                        unstructureData.getHost(), String.valueOf(unstructureData.getPort()), unstructureData.getDbname(),
                        unstructureData.getUsername(),
                        unstructureData.getPassword());
                if(ds==null){
                    return QueryResponse.genErr("无法获取数据库链接");
                }
                conn=ds.getConnection();
                if(conn==null){
                    log.error("无法获取数据库连接");
                    return QueryResponse.genErr("无法获取数据库链接");
                }
                String sql = String.format("show columns from %s;", tablename);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    if (!rs.getString("Field").equals("id")){
                        allfieldMap.put(rs.getString("Field"),rs.getString("Type"));//得到表中所有列名
                    }
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("allfieldMap", allfieldMap);
                jsonObject.put("id", unstructureData.getId());
                response.setSuccess(true);
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
                response.setMsg("开始同步设置！");
                log.info("根据数据库名和表名查所有字段名和类型成功！");
            }catch (SQLException e){
                e.printStackTrace();
                log.info("数据库连接错误,错误信息：",e);
            }
        }
        return response;
    }


    /**
     * @Description 新建同步表
     * @param id ID
     * @param fieldlist Field列表
     * @return QueryResponse
     */
    public QueryResponse createNewSynchronizeTable(Integer id,List<Field> fieldlist){
        QueryResponse response = new QueryResponse();
        String field;
        String fieldtype;
        String isdatasource;
        //判断同步列JSON是否符合要求
        for(Field fie:fieldlist){
            String updatetime= fie.getUpdatetime();
            String title=fie.getTitle();
            String html=fie.getHtml();
            String datasource=fie.getIsdatasource();
            if(updatetime.equals("1")&&(title.equals("1")||html.equals("1")||datasource.equals("1"))){
                log.info("每行只能选择一个！");
                return QueryResponse.genErr("每行只能选择一个！");
            }else if(title.equals("1") && (html.equals("1") || datasource.equals("1"))){
                log.info("每行只能选择一个！");
                return QueryResponse.genErr("每行只能选择一个！");
            }else if(html.equals("1") && datasource.equals("1")){
                log.info("每行只能选择一个！");
                return QueryResponse.genErr("每行只能选择一个！");
            }else {
                log.info("选择正确！");
            }
        }
        //将update_time列和html列转为json字符串存入同步列表中
        String synjson = JSON.toJSONString(fieldlist);
        //根据ID查询非结构化数据源
        UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        if(unstructureData==null){
            log.info("根据ID未查询到数据源！");
            return QueryResponse.genErr("根据ID未查询到数据源！");
        }
        String host=unstructureData.getHost();
        String tablename=unstructureData.getTablename();
        String syncTable=unstructureData.getSyncTable();
        UnstructureData unstruct=new UnstructureData();
        Timestamp date=Timestamp.valueOf("2000-1-1 0:0:0");//得到2000-1-1 0:0:0时间戳，为了在同步数据的时候插入这个时间段以后的数据，这样第一次同步和以后的同步可以是同一个操作
        unstruct.setSynchronizePosition(date);
        unstruct.setId(id);
        if (host.equals("localhost")){
            unstruct.setSynList(synjson);
            unstruct.setSyncTable(tablename);
            int isOK= unstructureDataMapper.updateUnstructureDataById(unstruct);
            if(isOK!=0){
                log.info("更新非结构化数据源同步列表和同步表名成功");
                response.setSuccess(true);
                response.setMsg("更新非结构化数据源同步列表和同步表名成功！");
            }
        }else{
            unstruct.setSynList(synjson);
            int isOk= unstructureDataMapper.updateUnstructureDataById(unstruct);
            if(isOk!=0){
                log.info("更新非结构化数据源同步列表成功");
            }
            //从非结构数据源中读取synlist中的同步字段信息
            String synlistStr=unstructureDataMapper.selectSynlistByDbnameAndTablename(id);
            List<Field> synli = JSONArray.parseArray(synlistStr, Field.class);
            //检验同步表是否存在
            int istableexist = unstructureDataMapper.isSynchronizeTableExist(syncTable);
            if (istableexist != 0){
                log.info("该同步表已创建！");
//                return QueryResponse.genErr("该同步表已创建！");
            }
            //数据库中删除原同步表
            String syncTab=unstructureData.getSyncTable();
            if (!(syncTab==null)){//如果同步表字段有值，则删除原同步表（为了编辑同步设置时，删除原表并新建表）
                int  droptab= unstructureDataMapper.dropTableByname(syncTab);
                if(droptab!=0){
                    log.info("同步删除失败!");
                    return QueryResponse.genErr("同步删除失败！");
                }
                //同步状态置为0：未同步
                unstructureDataMapper.updateStateBySchema(id,0);
            }
            //sync_+五位随机数得到表名
            String synchronizetablename="sync_"+GetRandomChar.getRandom(5);
            //将同步表名存入非结构化数据源表中
            int isok=unstructureDataMapper.updateSynctableField(synchronizetablename,id);
            if(isok==0){
                log.info("存入同步表名失败！");
                return QueryResponse.genErr("存入同步表名失败！");
            }
            //新建同步表
            int table=unstructureDataMapper.addNewSynchronizeTable(synchronizetablename);
            if(table!=0){
                log.info("同步表新建失败!");
                return QueryResponse.genErr("同步表新建失败！");
            }
            log.info("同步表新建成功！");
            String title;
            String updatetime;
            String html;
            for (Field f : synli) {
                field = f.getField();
                fieldtype = f.getFieldtype();
                isdatasource = f.getIsdatasource();
                title=f.getTitle();
                updatetime=f.getUpdatetime();
                html=f.getHtml();
                if (isdatasource.equals("0")) {//如果不是数据源字段（因为数据源字段只在筛选指定数据源数据时用到）
                    if (title.equals("1")){
                        field="title";//统一标题字段为title
                    }
                    if (updatetime.equals("1")){
                        field="update_time";//统一更新时间字段为update_time
                        fieldtype="timestamp";//统一update_time为时间戳类型
                    }
                    if (html.equals("1")){
                        field="content";//统一内容字段为content
                    }
                    //新表中添加数据源中待同步的字段
                    int ok=unstructureDataMapper.alterNewField(synchronizetablename, field, fieldtype);
                    if (ok!=0){
                        log.info("新表中添加数据源中待同步的字段失败!");
                        return QueryResponse.genErr("新表中添加数据源中待同步的字段失败！");
                    }
                }
            }
            response.setSuccess(true);
            response.setMsg("同步表新建成功！");
        }
        return response;
    }

    /**
     * @Description 同步表中的列
     * @param id ID
     * @return QueryResponse
     */
   public QueryResponse synchronizeField(Integer id) {
     return synchronize(id);
    }

    /**
     * @Description  开始自动同步
     * @param id 数据源ID
     * @return QueryResponse
     */
   public QueryResponse startAutoSync(Integer id) {
       QueryResponse response = new QueryResponse();
       try{
           quartzManager.addJob(id);
           int isok = unstructureDataMapper.changeTaskFlagStart(id);
           if(isok!=0){
               response.setSuccess(true);
               response.setMsg("开始定时同步成功！");
           }
       } catch (Exception e) {
           e.printStackTrace();
           response.setSuccess(false);
           response.setMsg("开始定时同步失败！");
       }
       return response;
   }


    /**
     * 暂停定时同步（把同步状态设为5：暂停同步，并未立即杀死同步任务）
     * @param id 数据源ID
     * @return response
     */
    public QueryResponse stopAutoSync(Integer id){
        QueryResponse response = new QueryResponse();
        try{
            quartzManager.removeJob(id);
            //更新同步状态为5：暂停同步
            int tap = unstructureDataMapper.updateStateBySchema(id,5);
            int isok = unstructureDataMapper.changeTaskFlagEnd(id);
            log.info("--------定时任务关闭 ! ------------");
            response.setSuccess(true);
            if(tap != 0&& isok !=0){
                response.setMsg("暂停同步成功！");
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMsg("暂停同步失败！");
            e.printStackTrace();
        }
        return response;
    }


    /**
     * @Description 查看所有数据源
     * @param datasourcetype 数据源类型
     * @param synchronizeState 同步周期
     * @param isdelete 是否删除
     * @param dataSourceName 数据源名
     * @param pageNumber 页数
     * @param recordsPerPage 展示页数
     * @return QueryResponse
     */
    public QueryResponse selectDatasource(String datasourcetype,String synchronizeState,String isdelete,String dataSourceName,int pageNumber,int recordsPerPage) throws JsonProcessingException {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        int lsn=(pageNumber-1)*recordsPerPage;//从第几条开始查找
        dataSourceName=dataSourceName.trim();
        if (!dataSourceName.equals("")){
            dataSourceName="%"+dataSourceName+"%";
        }
        int allpage;
        int add;
        synchronizeState=synchronizeState.replace("|",",");
        isdelete=isdelete.replace("|",",");
        if(datasourcetype.equals("0")){//datasourcetype=0是结构化数据
            //根据同步状态和时候删除查看结构化数据
            List<StructureData> allstructdatalist=selectStructDatasources(synchronizeState,isdelete,dataSourceName,lsn,recordsPerPage);
            if(allstructdatalist.isEmpty()){
                log.info("未查询到结构化数据源！");
                return QueryResponse.genErr("未查询到结构化数据源！");
            }
            //根据同步状态和时候删除查看结构化数据数量
            int total=selectStructDatasourceNumbers(synchronizeState,isdelete,dataSourceName);
            add=total%recordsPerPage;
            if (add==0){
                allpage=total/recordsPerPage;
            }else {
                allpage=total/recordsPerPage+1;
            }
            jsonObject.put("total", total);
            jsonObject.put("allpage", allpage);
            jsonObject.put("alldatalist", allstructdatalist);
            log.info("查询结构化数据源成功！");

        }else if(datasourcetype.equals("1")){//datasourcetype=1是非结构化数据
            //根据同步状态和时候删除查看非结构化数据
            List<UnstructureData> allunstructdatalist=selectUnstructDatasources(synchronizeState,isdelete,dataSourceName,lsn,recordsPerPage);
            if(allunstructdatalist.isEmpty()) {
                log.info("未查询到非结构化数据源！");
                return QueryResponse.genErr("未查询到非结构化数据源！");
            }
            //根据同步状态和时候删除查看非结构化数据数量
            int total=selectUnstructDatasourceNumbers(synchronizeState,isdelete,dataSourceName);
            add=total%recordsPerPage;
            if (add==0){
                allpage=total/recordsPerPage;
            }else {
                allpage=total/recordsPerPage+1;
            }
            jsonObject.put("total", total);
            jsonObject.put("allpage", allpage);
            jsonObject.put("alldatalist", allunstructdatalist);
            log.info("查询非结构化数据源成功！");
        }else  {//datasourcetype=2是全部数据
            //根据同步状态和时候删除查看结构化数据
            List<StructureData> allstructdatalist=selectStructDatasources(synchronizeState,isdelete,dataSourceName,lsn,recordsPerPage);
            if(allstructdatalist==null || allstructdatalist.isEmpty()){
                log.info("未查询结构化数据源！");
            }
            //根据同步状态和时候删除查看结构化数据数量
            int structtotal=selectStructDatasourceNumbers(synchronizeState,isdelete,dataSourceName);
            //根据同步状态和时候删除查看非结构化数据
            List<UnstructureData> allunstructdatalist=selectUnstructDatasources(synchronizeState,isdelete,dataSourceName,lsn,recordsPerPage);
            if(allunstructdatalist==null || allunstructdatalist.isEmpty()){
                log.info("未查询到非结构化数据源！");
            }
            //根据同步状态和时候删除查看非结构化数据数量
            int unstructtotal=selectUnstructDatasourceNumbers(synchronizeState,isdelete,dataSourceName);
            if ((allunstructdatalist==null || allunstructdatalist.isEmpty())&&(allstructdatalist==null || allstructdatalist.isEmpty())){
                log.info("未查询到数据源！");
                return QueryResponse.genErr("未查询到数据源！");
            }
           int total=structtotal+unstructtotal;//将结构化数据和非结构化数据数量相加
            add=total%recordsPerPage;//一共几页
            if (add==0){
                allpage=total/recordsPerPage;
            }else {
                allpage=total/recordsPerPage+1;
            }
            jsonObject.put("total", total);
            jsonObject.put("allpage", allpage);
            List<Object> alldatalist=new ArrayList<>();
            assert allstructdatalist != null;
            if (!allstructdatalist.isEmpty()){//如果结构化数据不为空，将结构化数据放入alldatalist
                alldatalist.addAll(allstructdatalist);
            }
            assert allunstructdatalist != null;
            if(!allunstructdatalist.isEmpty()){//如果非结构化数据不为空，将非结构化数据放入alldatalist
                alldatalist.addAll(allunstructdatalist);
            }
            jsonObject.put("alldatalist", alldatalist);
        }
        response.setSuccess(true);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = objectMapper.writeValueAsString(jsonObject);
        response.setJson(jsonStr);
        log.info("查询数据源成功！");
        response.setMsg("查询数据源成功！");
        return response;
    }

    /**
     * @Description 根据id删除数据源(假删除)
     * @param id ID
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    public QueryResponse deleteDatasourceById(Integer id, int datasourcetype){
        QueryResponse response = new QueryResponse();
        int isOk;
        if(datasourcetype==1){//非结构化数据
            isOk = unstructureDataMapper.updateIsdeleteById(id);
        }else {//结构化数据
            isOk = structureDataMapper.updateIsdeleteById(id);
        }
        if (isOk == 0){
            log.info("根据ID更新isdelete失败！");
            return QueryResponse.genErr("根据ID更新isdelete失败！");
        }
        log.info("根据ID更新isdelete成功！");
        response.setSuccess(true);
        response.setMsg("根据ID更新isdelete成功！");
        return response;
    }

    /**
     * @Description 根据ID查询数据源
     * @param id ID
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    public QueryResponse selectDatasourceById(Integer id, int datasourcetype){
       QueryResponse response = new QueryResponse();
       JSONObject jsonObject = new JSONObject();
       if(datasourcetype==1){//非结构化数据
           UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(id);
           if (unstructureData == null ){
               log.info("根据ID未查询到非结构化数据源！");
               return QueryResponse.genErr("根据ID未查询到非结构化数据源！");
           }
           jsonObject.put("unstructdatamap", unstructureData);
       }else {//结构化数据
           StructureData structureData=structureDataMapper.selecStructDataDatasourceById(id);
           if (structureData == null ){
               log.info("根据ID未查询结到构化数据源！");
               return QueryResponse.genErr("根据ID未查询结到构化数据源！");
           }
           jsonObject.put("structdatamap", structureData);
       }
       response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
       log.info("根据ID查询数据源成功！");
       response.setSuccess(true);
       response.setMsg("根据ID查询数据源成功！");
       return response;
   }

    /**
     * 根据id更新数据源
     * @param id ID
     * @param dataSourceName 数据源名
     * @param synchronizeCycle 同步周期
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
    public QueryResponse editorsDatasourceById(Integer id,String dataSourceName,Integer synchronizeCycle,int datasourcetype){
        QueryResponse response = new QueryResponse();
        dataSourceName=dataSourceName.trim();
        if (datasourcetype==0){//结构化数据
            StructureData structureData=new StructureData();
            structureData.setId(id);
            structureData.setSynchronizeCycle(synchronizeCycle);
            structureData.setDataSourceName(dataSourceName);
            int isOk=structureDataMapper.updateStructureDataById(structureData);
            if (isOk==0){
                log.info("根据ID更新结构化数据源表同步周期失败！");
                return QueryResponse.genErr("根据ID更新结构化数据源表同步周期失败！");
            }
            log.info("根据ID更新结构化数据源表同步周期成功！");
            response.setSuccess(true);
            response.setMsg("根据ID更新结构化数据源表同步周期成功！");
        }else {//非结构化数据
            UnstructureData unstructureData=new UnstructureData();
            unstructureData.setId(id);
            unstructureData.setSynchronizeCycle(synchronizeCycle);
            int isOk=unstructureDataMapper.updateUnstructureDataById(unstructureData);
            if (isOk==0){
                log.info("根据ID更新非结构化数据源表同步周期失败！");
                return QueryResponse.genErr("根据ID更新非结构化数据源表同步周期失败！");
            }
            log.info("根据ID更新非结构化数据源表同步周期成功！");
            response.setSuccess(true);
            response.setMsg("根据ID更新非结构化数据源表同步周期成功！");
        }
        return response;
    }


    /**
     * 查看同步设置，根据ID返回syn_list列
     * @param id ID
     * @param datasourcetype 数据源类型
     * @return QueryResponse
     */
   public QueryResponse viewSyncSettings(Integer id,int datasourcetype){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String synlist;
        List<Field> syncFieldlist;
        if(datasourcetype==1){//非结构化数据
            UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(id);
            if (unstructureData == null ){
                log.info("根据ID未查询到非结构化数据源！");
                return QueryResponse.genErr("根据ID未查询到非结构化数据源！");
            }
            synlist=unstructureData.getSynList();
        }else {//结构化数据
            StructureData structureData=structureDataMapper.selecStructDataDatasourceById(id);
            if (structureData == null ){
                log.info("根据ID未查询到结构化数据源！");
                return QueryResponse.genErr("根据ID未查询到结构化数据源！");
            }
            synlist=structureData.getSynList();
        }
        syncFieldlist = JSONArray.parseArray(synlist, Field.class);
        jsonObject.put("syncFieldlist", syncFieldlist);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        log.info("根据ID查看同步设置成功！");
        response.setSuccess(true);
        response.setMsg("根据ID查看同步设置源成功！");
        return response;
    }



    /**
     * 查询数据源名
     * @return QueryResponse
     */
   public  QueryResponse selectDatasourceName(){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<StructureData> allstructdatalist=structureDataMapper.selecStructureDatasourceName();//查询结构化数据
        List<Map<String,String>> structdatalist=new ArrayList<>();
        if (allstructdatalist.isEmpty()){
            log.info("未查到结构化数据源！");
        }else {
            for(StructureData structureData:allstructdatalist){
                Map<String,String> structdatasourceNameMap=new HashMap<>();
                String datasourceName=structureData.getDataSourceName();
                structdatasourceNameMap.put("datasourceName",datasourceName);
                String id=structureData.getId().toString();
                structdatasourceNameMap.put("id",id);
                String dataType=structureData.getDatatype().toString();
                structdatasourceNameMap.put("dataType",dataType);
                structdatalist.add(structdatasourceNameMap);
            }
        }
        List<UnstructureData> allunstructdatalist=unstructureDataMapper.selectUnstructureDatasourceName();//查询非结构化数据
        List<Map<String,String>> unstructdatalist=new ArrayList<>();
        if (allunstructdatalist.isEmpty()){
            log.info("未查到非结构化数据源！");
        }else {
            for(UnstructureData unstructureData:allunstructdatalist){
                Map<String,String> unstructdatasourceNameMap=new HashMap<>();
                String datasourceName=unstructureData.getDataSourceName();
                unstructdatasourceNameMap.put("datasourceName",datasourceName);
                String id=unstructureData.getId().toString();
                unstructdatasourceNameMap.put("id",id);
                String dataType=unstructureData.getDatatype().toString();
                unstructdatasourceNameMap.put("dataType",dataType);
                unstructdatalist.add(unstructdatasourceNameMap);
            }
        }
        if (allunstructdatalist.isEmpty()&&allstructdatalist.isEmpty()){//结构化数据源和非结构化数据源同时为空
            log.info("未查询到数据源！");
            return QueryResponse.genErr("未查询到数据源！");
        }else {
            List<Map<String,String>> alldataNameList=new ArrayList<>();
            alldataNameList.addAll(structdatalist);
            alldataNameList.addAll(unstructdatalist);
            jsonObject.put("alldataNameList", alldataNameList);
            log.info("查询所有数据源名成功！");
            response.setSuccess(true);
            response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
            response.setMsg("查询所有数据源名成功！");
        }
        return response;
    }


    /**
     * 从爬虫库中读取数据并插入新库
     * @param sql 插入sql语句
     * @param fieList 字段列表
     * @param map <旧字段名，新字段名>，用于同步时字段值映射到固定的字段（title，content，update_time）中
     * @param syncTable 同步表名
     * @param unstructureData 非结构化数据
     */
   private void readAndInsert(String sql, List<String> fieList,  Map<String,String> map, String update_time, String syncTable, UnstructureData unstructureData) {
        Connection conn;
        try {
            int id = unstructureData.getId();
            DataSource ds=DataSourceFactory.getDataSource(unstructureData.getDbtype(),
                    unstructureData.getHost(), String.valueOf(unstructureData.getPort()), unstructureData.getDbname(),
                    unstructureData.getUsername(),
                    unstructureData.getPassword());
            if (ds==null){
                log.error("无法获取数据库连接");
                return;
            }
            conn=ds.getConnection();
            if(conn==null){
                log.error("无法获取数据库连接");
                return;
            }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Map<String,String>> allfieldli=new ArrayList<>();
            while (rs.next()) {
                Map<String,String> fieldMap = new HashMap<>();
                for(String fie:fieList){
                    String fieldvalue=rs.getString(fie);
                    fieldMap.put(fie,fieldvalue);
                }
                allfieldli.add(fieldMap);
            }
            log.info("从爬虫库读取数据成功！");
            //插入新表
            for(Map<String ,String> fiemap : allfieldli){
                StringBuilder fieKey= new StringBuilder();
                StringBuilder fieValue= new StringBuilder();
                List<String> keys = new ArrayList<>(fiemap.keySet());//爬虫表中待同步的所有字段名list
                for(String ma : map.keySet()){//原字段名和新字段名的map{“更新时间”：“update_time”,"标题”：”title“,"内容":"content"}，将待同步字段中的字段替换为统一的
                    int i = keys.indexOf(ma);
                    keys.set(i, map.get(ma));
                }
                for (String key:keys){
                    fieKey.append(key).append(",");//将表中待插入字段名放入一个字符串中，用逗号分开
                }
                fieKey = new StringBuilder(fieKey.substring(0, fieKey.length() - 1));//删除最后一个逗号
                Collection<String> values = fiemap.values();
                for (String value:values){
                    if (value==null){
                        continue;
                    }
                    fieValue.append("'").append(value.replace("'", "\\'").trim()).append("'").append(","); //将表中待插入字段值放入一个字符串中，用逗号分开，将爬虫表数据中引号转义
                }
                fieValue = new StringBuilder(fieValue.substring(0, fieValue.length() - 1));//删除最后一个逗号nto
                int isok= unstructureDataMapper.insertNewField(syncTable, fieKey.toString(), fieValue.toString());
                if (isok == 0){
                    log.info("同步表中插入需要同步的字段值失败！");
                }
                String synposition = fiemap.get(update_time);
                int ok= unstructureDataMapper.updatePositon(id,synposition);
                if (ok==0){
                   log.info("根据表名更新同步位置失败！");
                }
            }
            log.info("同步表同步成功！");
        }catch (SQLException e){
            e.printStackTrace();
            log.info("数据库连接错误,错误信息：",e);
        }
   }

    /**
     *  同步数据
     * @param id 数据源ID
     * @return QueryResponse
     */
   private QueryResponse synchronize(Integer id){
       QueryResponse response = new QueryResponse();
       UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        if(unstructureData==null){
            log.info("根据ID未查询到数据源！");
            return QueryResponse.genErr("根据ID未查询到数据源！");
        }
       String tableName=unstructureData.getTablename();
       String field;
       String isdatasource;
       String datasouce = null;
       String filter = null;
       String syncPosition;//同步位置
       String update_time=null;//爬虫库的同步时间字段名
       String sql;
       int state = unstructureData.getSynchronizeState();
       if (state==1){
           log.info("正在同步中！");
           return QueryResponse.genErr("正在同步中！");
       }
        //同步状态设置为正在同步：1(根据ID)
        int isok= unstructureDataMapper.updateStateBySchema(id,1);
        if (isok==0){
            log.info("同步状态设置为正在同步失败！");
            return QueryResponse.genErr("同步状态设置为正在同步失败！");
        }
        //从非结构数据源中读取synlist中的同步字段信息
        String synlistStr=unstructureDataMapper.selectSynlistByDbnameAndTablename(id);
        List<Field> synli = JSONArray.parseArray(synlistStr, Field.class);
        //检验同步表是否存在
        String syncTable = unstructureData.getSyncTable();
        if (syncTable==null|| syncTable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        //判断数据库中是否存在该表
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(syncTable);
        if (istableexist==0){
            log.info("同步表不存在！！");
            return QueryResponse.genErr("同步表不存在！");
        }
        //判断同步位置
        syncPosition=unstructureDataMapper.getSyncPosition(id);
        log.info("同步位置"+":"+syncPosition);
        //读取过滤条件和时间戳字段，将除过滤条件字段的其他字段放入一个list并转为用“,"分开的字符串
        List<String>  fieList = new ArrayList<>();
        Map<String,String> map = new HashMap<>();//<旧字段名，新字段名>，用于同步时字段值映射到固定的字段（title，content，update_time）中
        for(Field f : synli){
           field = f.getField();
           String isUpdateTime = f.getUpdatetime();
           String isTitle = f.getTitle();
           String isContent = f.getHtml();
           isdatasource=f.getIsdatasource();
           if(isdatasource.equals("1")){//判断是否为数据源字段，如果为数据源字段，过滤条件=数据源字段名，数据源为用户输入的数据源名
               filter = field;
               datasouce = f.getDatasource();
           }else {//如果不是数据源字段
               if(isUpdateTime.equals("1")){//是否是update_time字段
                   update_time = field;
                   if(!f.getFieldtype().equals("timestamp")){//由于武器百科中的update_time字段类型为text（2019/6/8 00:00:00）,所以需要修改同步位置的时间戳类型格式（2019-06-08 00:00:00）
                       //把2019-06-08 00:00:00格式改为  2019/6/8 00:00:00
                       syncPosition = syncPosition.replace("-","/").replace("/0","/");
                   }
                   if(!field.equals("update_time")){
                       map.put(update_time, "update_time");
                   }
               } else if(isTitle.equals("1")&&!field.equals("title")){//是否是title字段
                   map.put(field, "title");
               } else if(isContent.equals("1")&&!field.equals("content")){//是否是content字段
                   map.put(field, "content");
               }
               fieList.add(field);//fieList中加入待同步的字段名
           }
        }
        String fieldStr = String.join(",", fieList);//将字段名转为","分开的字符串
        if(filter!=null) {//有数据源列的筛选条件
           sql="select "+fieldStr+" from "+tableName+" where "+filter+" like '%"+datasouce+"%'"+"and "+update_time+">"+ "'" +syncPosition+ "' order by "+update_time+" asc";
        }else{//无数据源列的筛选条件
           sql="select "+fieldStr+" from "+tableName+" where "+update_time+">'" +syncPosition+ "' order by "+update_time+" asc";
        }
        //将对应字段源数据同步到新的表中
        readAndInsert(sql, fieList, map, update_time, syncTable, unstructureData);
        //同步状态设置为正在同步：2（根据表名新同步状态）根据表名
        unstructureDataMapper.updateStateBySchema(id,2);
        response.setMsg("同步表同步成功！");
        response.setSuccess(true);
        return response;
   }


    /**
     * 根据同步状态和时候删除查看结构化数据
     * @param synchronizeState 同步状态
     * @param isdelete 是否删除
     * @param dataSourceName 数据源名
     * @param lsn 第几个开始返回
     * @param displayNumber 显示页数
     * @return allstructdatalist
     */
    private List<StructureData> selectStructDatasources(String synchronizeState,String isdelete,String dataSourceName,int lsn,int displayNumber){
        List<StructureData> allstructdatalist;
        if (!synchronizeState.equals("")&&!isdelete.equals("")){
            allstructdatalist=structureDataMapper.selectStructDatasource(synchronizeState,isdelete,dataSourceName,lsn,displayNumber);
        }else if (synchronizeState.equals("")&&!isdelete.equals("")){
            allstructdatalist=structureDataMapper.selectStructDatasource("0,1,2,3,4,5",isdelete,dataSourceName,lsn,displayNumber);
        }else if (!synchronizeState.equals("")){
            allstructdatalist=structureDataMapper.selectStructDatasource(synchronizeState,"0,1",dataSourceName,lsn,displayNumber);
        }else {
            allstructdatalist=structureDataMapper.selectStructDatasource("0,1,2,3,4,5","0,1",dataSourceName,lsn,displayNumber);
        }
        return allstructdatalist;
    }


    /**
     * 根据同步状态和时候删除查看非结构化数据
     * @param synchronizeState 同步状态
     * @param isdelete 是否删除
     * @param dataSourceName 数据源名
     * @param lsn 第几个开始返回
     * @param displayNumber 显示页数
     * @return allunstructdatalist
     */
    private List<UnstructureData> selectUnstructDatasources(String synchronizeState,String isdelete,String dataSourceName,int lsn,int displayNumber){
        List<UnstructureData> allunstructdatalist;
        if(!synchronizeState.equals("")&&!isdelete.equals("")){
            allunstructdatalist=unstructureDataMapper.selectUnstructDatasource(synchronizeState,isdelete,dataSourceName,lsn,displayNumber);
        }else if (synchronizeState.equals("")&&!isdelete.equals("")){
            allunstructdatalist=unstructureDataMapper.selectUnstructDatasource("0,1,2,3,4,5",isdelete,dataSourceName,lsn,displayNumber);
        }else if (!synchronizeState.equals("")){
            allunstructdatalist=unstructureDataMapper.selectUnstructDatasource(synchronizeState,"0,1",dataSourceName,lsn,displayNumber);
        }else {
            allunstructdatalist=unstructureDataMapper.selectUnstructDatasource("0,1,2,3,4,5","0,1",dataSourceName,lsn,displayNumber);
        }
        return allunstructdatalist;
    }


    /**
     * 根据同步状态和时候删除查看结构化数据数量
     * @param synchronizeState 同步状态
     * @param isdelete 是否删除
     * @param dataSourceName 数据源名
     * @return  total
     */
    private int selectStructDatasourceNumbers(String synchronizeState,String isdelete,String dataSourceName){
        int total;
        if (!synchronizeState.equals("")&&!isdelete.equals("")){
            total=structureDataMapper.selectStructDatasourceNumber(synchronizeState,isdelete,dataSourceName);
        }else if (synchronizeState.equals("")&&!isdelete.equals("")){
            total=structureDataMapper.selectStructDatasourceNumber("0,1,2,3,4,5",isdelete,dataSourceName);
        }else if (!synchronizeState.equals("")){
            total=structureDataMapper.selectStructDatasourceNumber(synchronizeState,"0,1",dataSourceName);
        }else {
            total=structureDataMapper.selectStructDatasourceNumber("0,1,2,3,4,5","0,1",dataSourceName);
        }
        return total;
    }


    /**
     * 根据同步状态和时候删除查看非结构化数据数量
     * @param synchronizeState 同步状态
     * @param isdelete 是否删除
     * @param dataSourceName 数据源名
     * @return  total
     */
    private int selectUnstructDatasourceNumbers(String synchronizeState,String isdelete,String dataSourceName){
        int total;
        if(!synchronizeState.equals("")&&!isdelete.equals("")){
            total=unstructureDataMapper.selectUnstructDatasourceNumber(synchronizeState,isdelete,dataSourceName);
        }else if (synchronizeState.equals("")&&!isdelete.equals("")){
            total=unstructureDataMapper.selectUnstructDatasourceNumber("0,1,2,3,4,5",isdelete,dataSourceName);
        }else if (!synchronizeState.equals("")){
            total=unstructureDataMapper.selectUnstructDatasourceNumber(synchronizeState,"0,1",dataSourceName);
        }else {
            total=unstructureDataMapper.selectUnstructDatasourceNumber("0,1,2,3,4,5","0,1",dataSourceName);
        }
        return total;
    }
}
