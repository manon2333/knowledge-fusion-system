package com.edu.bistu.datacollectproofaudit.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.mapper.UnstructureDataMapper;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.pojo.Field;
import com.edu.bistu.datacollectproofaudit.pojo.UnstructureData;
import com.edu.bistu.datacollectproofaudit.pojo.User;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistributionService{

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    private final UnstructureDataMapper unstructureDataMapper;

    private final UserMapper userMapper;

    public DistributionService(@Autowired UnstructureDataMapper unstructureDataMapper,
                               @Autowired UserMapper userMapper){
        this.unstructureDataMapper = unstructureDataMapper;
        this.userMapper=userMapper;
    }

    /**
     * 查询所有非结构化数据源中的数据源名称列
     * @return QueryResponse
     */
    public QueryResponse selectUnstructureDatasource(){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<Map<String,String>> datasourcenamelist=new ArrayList<>();
        List<UnstructureData> datasourcelist = unstructureDataMapper.selectUnstructureDatasourceName();
        if(null == datasourcelist || datasourcelist.size() == 0 ){
             log.info("未查询到数据源名称！");
             return QueryResponse.genErr("未查询到数据源名称!");
        }
        for(UnstructureData datasource:datasourcelist){
            Map<String,String> unstructdatasourceNameMap=new HashMap<>();
            String datasourceName=datasource.getDataSourceName();
            unstructdatasourceNameMap.put("datasourceName",datasourceName);
            String id=datasource.getId().toString();
            unstructdatasourceNameMap.put("id",id);
            datasourcenamelist.add(unstructdatasourceNameMap);
        }
        jsonObject.put("datasourcenamelist", datasourcenamelist);
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询数据源名称成功！");
        log.info("查询数据源名称成功！");
        return response;
    }

    /**
     * 查询校对人
     * @return QueryResponse
     */
    public QueryResponse selectproofreader(){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<String> usernamelist=new ArrayList<>();
        //从用户表查询所有校对人
        List<User> userlist = userMapper.selectAllUsername();
        if(null == userlist || userlist.size() ==0 ){
            log.info("未查询到所有用户名！");
            return QueryResponse.genErr("未查询到所有用户名!");
        }
        for(User user:userlist){
            usernamelist.add(user.getUsername());
        }
        jsonObject.put("usernamelist", usernamelist);
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询所有用户名成功！");
        log.info("查询所有用户名成功！");
        return response;
    }

    /**
     * 查询数据源对应的未分配的数据
     * @param id 数据源ID
     * @param pageNumber 第几页
     * @param recordsPerPage 展示页数
     * @return QueryResponse
     */
    public QueryResponse selectdata(Integer id,int pageNumber,int recordsPerPage){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        int lsn=(pageNumber-1)*recordsPerPage;//sql语句中，limit后的开始位置数。
        //根据数据源名查询同步表名
        UnstructureData unstructure=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        String synchronizetable=unstructure.getSyncTable();
        String unstructDataName = unstructure.getDataSourceName();
        if (synchronizetable==null|| synchronizetable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
        if (istableexist==0){
            log.info("该数据源对应的同步表不存在!");
            return QueryResponse.genErr("该数据源对应的同步表不存在！");
        }
        String synList = unstructure.getSynList();
        List<Field> synLi = JSON.parseObject(synList, new TypeReference<ArrayList<Field>>() {});
      //  String title="";
        String dataSource = "";
      //  String updateTime = "";
        for(Field f : synLi){
          //  String istitle=f.getTitle();
            String isdatasource = f.getIsdatasource();
          //  String isUpdateTime = f.getUpdatetime();
//            if(istitle.equals("1")){
//                title = f.getField();
//            }
            if(isdatasource.equals("1")){
                dataSource=f.getField();
            }
//            if(isUpdateTime.equals("1")){
//                updateTime = f.getField();
//            }
        }
        //找到对应同步表并查询同步表中的所有未分配数据源
        List<Map<String,Object>> unstructuredatalist =unstructureDataMapper.selectUnassignedUnstructureDataBySynchronizetablename(synchronizetable,lsn,recordsPerPage);
        int total=unstructureDataMapper.selectUnassignedUnstructureDataNumber(synchronizetable);//总记录
        if(unstructuredatalist==null || unstructuredatalist.size() ==0){
            log.info("未查询到同步表数据！");
            return QueryResponse.genErr("未查询到同步表数据!");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Map<String,Object> m:unstructuredatalist){
            Map<String,Object> map=new HashMap<>();
            map.put("id",m.get("id"));
            if (m.get("update_time")==null||m.get("update_time").equals("")){
                map.put("update_time","");
            }else {
                map.put("updateTime",m.get("update_time"));
            }
            if (m.get("title")==null||m.get("title").equals("")){
                map.put("title","");
            }else {
                map.put("title",m.get("title"));
            }
            if (m.get(dataSource)==null||m.get(dataSource).equals("")){
                map.put("dataSourceType","");
            }else {
                map.put("dataSourceType",m.get(dataSource));
            }
            if (unstructDataName==null||unstructDataName.equals("")){
                map.put("dataSource","");
            }else {
                map.put("dataSource",unstructDataName);
            }

            list.add(map);
        }
        jsonObject.put("list", list);
        int allpage;//总页数
        int add=total%recordsPerPage;
        if (add==0){
            allpage=total/recordsPerPage;
        }else {
            allpage=total/recordsPerPage+1;
        }
        jsonObject.put("total", total);
        jsonObject.put("allpage", allpage);
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("根据数据源名查询未分配非结构化数据源数据成功！");
        log.info("根据数据源名查询未分配非结构化数据源数据成功！");
        return response;
    }

    /**
     * 根据起止序号筛选未分配的数据（未用）
     * @param id 数据源ID
     * @param start 开始序号
     * @param end 结束序号
     * @param pageNumber 第几页
     * @param recordsPerPageNumber 展示页数
     * @return selectdataBySerialNumber
     */
    public QueryResponse selectdataBySerialNumber(Integer id,int start,int end,int pageNumber,int recordsPerPageNumber){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        int number = end-start;
        int pagingstart =(pageNumber-1)*recordsPerPageNumber;//分页开始
        int pagingend=pageNumber*recordsPerPageNumber;//分页结束
        //根据数据源名查询同步表名
        UnstructureData unstructure=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        String synchronizetable=unstructure.getSyncTable();
        if (synchronizetable==null|| synchronizetable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
        if (istableexist==0){
            log.info("该数据源对应的同步表不存在!");
            return QueryResponse.genErr("该数据源对应的同步表不存在！");
        }
        //通过同步表名找到对应同步表并查询同步表中的所有未分配数据源
        List<Map<String,Object>> unstructuredatalist =unstructureDataMapper.selectUnassignedUnstructureDataBySynchronizetablename(synchronizetable,start,number);
        int total=unstructureDataMapper.selectUnassignedUnstructureDataNumber(synchronizetable);//总记录
        int allpage=total/recordsPerPageNumber+1;//总页数
        List<Map<String,Object>> sectionunstructuredatalist=new ArrayList<>();
        if(unstructuredatalist.size()<recordsPerPageNumber){
            jsonObject.put("sectionunstructuredatalist", unstructuredatalist);
        }else {
            int i=0;
            for(Map<String,Object> map:unstructuredatalist){
                if(i>=pagingstart&&i<pagingend){
                    sectionunstructuredatalist.add(map);
                }
                i++;
            }
            jsonObject.put("sectionunstructuredatalist", sectionunstructuredatalist);
        }
        response.setSuccess(true);
        jsonObject.put("total", total);
        jsonObject.put("allpage", allpage);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("根据数据源名查询未分配非结构化数据源数据成功！");
        log.info("根据数据源名查询未分配非结构化数据源数据成功！");
        return response;
    }


    /**
     *根据起止序号更新同步表分配
     * @param id 数据源ID
     * @param proofreader 校对人
     * @param start 开始序号
     * @param end 结束序号
     * @return QueryResponse
     */
    public QueryResponse updateSerialNmberDistribution(Integer id,String proofreader,int start,int end){
        if (start>end){
            log.info("开始值应小于结束值！请重新输入！");
            return QueryResponse.genErr("开始值应小于结束值！请重新输入！");
        }
        if(start==end){
            log.info("开始值等于结束值！请重新输入！");
            return QueryResponse.genErr("开始值等于结束值！请重新输入！");
        }
        QueryResponse response = new QueryResponse();
        //根据数据源名查询同步表名
        UnstructureData unstructure=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        String synchronizetable=unstructure.getSyncTable();
        if (synchronizetable==null|| synchronizetable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
        if (istableexist==0){
            log.info("该数据源对应的同步表不存在!");
            return QueryResponse.genErr("该数据源对应的同步表不存在！");
        }
        int number = (end-start)+1;
        List<Map<String,Object>> unstructuredatalist =unstructureDataMapper.selectUnassignedUnstructureDataBySynchronizetablename(synchronizetable,start-1,number);
        if (unstructuredatalist.size()<((end-start)+1)){
            log.info("数据不足！");
            return QueryResponse.genErr("数据不足！数据库中还剩余"+unstructuredatalist.size()+"条未分配数据！");
        }
        List<Integer> ids=new ArrayList<>();
        for(Map<String,Object> m:unstructuredatalist){
           ids.add((Integer) m.get("id"));
        }
        //更新同步表中的是否分配,校对人
        for(Integer idd:ids){
            Timestamp scheduleTime = new Timestamp(System.currentTimeMillis());
            int updat=unstructureDataMapper.updateUnstructureDatadistributionById(synchronizetable,proofreader,idd,scheduleTime);
            if (updat==0){
                log.info("根据ID更新同步表分配状态失败！");
                return QueryResponse.genErr("根据ID更新同步表分配状态失败！");
            }
            log.info("更新id为"+idd+"的数据分配状态成功！");
            response.setSuccess(true);
            response.setMsg("根据ID更新同步表分配状态成功！");
        }
        response.setMsg("分配成功！");
        log.info("更新同步表中的是否分配,校对人以及分配时间字段成功！");
        return  response;
    }



    /**
     * 根据ID将同步表的是否分配字段置为1,并填入校对人和分配时间
     * @parm id 数据源ID
     * @param proofreader 校对人
     * @param ids ID列表
     * @return QueryResponse
     */
    public QueryResponse updateDistribution(Integer id,String proofreader, List<Integer> ids){
        if(proofreader.equals("")){
            log.info("校对人不能为空!");
            return QueryResponse.genErr("校对人不能为空！");
        }
        QueryResponse response = new QueryResponse();
        //根据数据源名查询同步表名
        UnstructureData unstructure=unstructureDataMapper.selectUnstructDataDatasourceById(id);
        String synchronizetable=unstructure.getSyncTable();
        if (synchronizetable==null|| synchronizetable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
        if (istableexist==0){
            log.info("该数据源对应的同步表不存在!");
            return QueryResponse.genErr("该数据源对应的同步表不存在！");
        }
        //更新同步表中的是否分配,校对人
        for(Integer idd:ids){
            Timestamp scheduleTime = new Timestamp(System.currentTimeMillis());
            int updat=unstructureDataMapper.updateUnstructureDatadistributionById(synchronizetable,proofreader,idd,scheduleTime);
            if (updat==0){
                log.info("根据ID更新同步表分配状态失败！");
                return QueryResponse.genErr("根据ID更新同步表分配状态失败！");
            }
            log.info("更新id为"+idd+"的数据分配状态成功！");
            response.setSuccess(true);
            response.setMsg("根据ID更新同步表分配状态成功！");
        }
        response.setMsg("分配成功！");
        log.info("更新同步表中的是否分配,校对人以及分配时间字段成功！");
        return  response;
    }

}
