package com.edu.bistu.datacollectproofaudit.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.config.SysConfig;
import com.edu.bistu.datacollectproofaudit.mapper.UnstructureDataMapper;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.mapper.VerifyMapper;
import com.edu.bistu.datacollectproofaudit.metadata.MetadataService;
import com.edu.bistu.datacollectproofaudit.metadata.PropertyType;
import com.edu.bistu.datacollectproofaudit.metadata.RelationType;
import com.edu.bistu.datacollectproofaudit.pojo.*;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.edu.bistu.datacollectproofaudit.utils.StringUtil;
import com.edu.bistu.datacollectproofaudit.utils.TokenUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProofreadingService{

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    private final UnstructureDataMapper unstructureDataMapper;

    private final VerifyMapper verifyMapper;

    private final UserMapper userMapper;

    private final Retrofit retrofit;

    private final MetadataService metadataService;

    private List<Map<String,String>>  leafs;


    public ProofreadingService(@Autowired SysConfig sysConfig,
                               @Autowired UnstructureDataMapper unstructureDataMapper,
                               @Autowired VerifyMapper verifyMapper,
                               @Autowired UserMapper userMapper,
                               @Autowired MetadataService metadataService,
                               List<Map<String, String>> leafs){
        this.unstructureDataMapper = unstructureDataMapper;
        this.verifyMapper=verifyMapper;
        this.userMapper=userMapper;
        this.metadataService=metadataService;
        this.retrofit=new Retrofit.Builder()
                .baseUrl(sysConfig.getMetaservice_ip()+"/meta/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.leafs = leafs;
    }


    /**
     * 查询所有校对人名
     * @return QueryResponse
     */
    public QueryResponse selectProofreader(){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<User> userList=userMapper.selectAllUsername();
        if(userList==null || userList.size() ==0){
            log.info("未查询到用户数据！");
            return QueryResponse.genErr("未查询到用户数据!");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(User u:userList){
            Map<String, Object> map = new HashMap<>();
            String id = String.valueOf(u.getId());
            String name=u.getUsername();
            map.put("id",id);
            map.put("username",name);
            list.add(map);
        }
        jsonObject.put("userNameList", list);
        log.info("查询所有校对人成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询所有校对人成功！");
        return response;
    }

    /**
     * 根据数据源名，关键字，操作人，校对状态，采集时间，校对时间，校对类型查询数据
     * @param datasourcetype 数据源类型
     * @param id ID
     * @param keywords 关键词
     * @param proofreader 校对人
     * @param status 状态 0：未标注；1：已标注，2:已通过；3：已打回；4：已返工
     * @param acquisitionTimeStart 采集开始时间
     * @param acquisitionTimeEnd 采集结束时间
     * @param proofreadingTimeStart 校对开始时间
     * @param proofreadingTimeEnd 校对结束时间
     * @param pageNumber 第几页
     * @param recordsPerPage 展示页数
     * @param user 用户名
     * @return QueryResponse
     */
   public QueryResponse viewDatasource(int datasourcetype,int id, String keywords, String proofreader, int status, Date acquisitionTimeStart,
                                       Date acquisitionTimeEnd,Date proofreadingTimeStart,Date proofreadingTimeEnd, int pageNumber, int recordsPerPage,String user){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        keywords=keywords.trim();
        if (datasourcetype!=1){
            log.info("暂无数据！");
            return QueryResponse.genErr("暂无数据!");
        }
        int lsn=(pageNumber-1)*recordsPerPage;
        UnstructureData unstructureData = unstructureDataMapper.selectUnstructDataDatasourceById(id);
        String synchronizetable=unstructureData.getSyncTable();
        if (synchronizetable==null|| synchronizetable.equals("")){
            log.info("该同步表不存在！");
            return QueryResponse.genErr("该同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
        if (istableexist==0){
            log.info("该数据源对应的同步表不存在!");
            return QueryResponse.genErr("该数据源对应的同步表不存在！");
        }
        // 查数据源表中同步数据列字段数据
        if (!keywords.equals("")){
            keywords="%"+keywords+"%";
        }
        //根据标题、关键字、校对人、是否校对、更新时间、采集时间和校对时间及用户筛选同步表中的数据
        List<Map<String,Object>> synchronizetablelist;
        int total;
        synchronizetablelist = unstructureDataMapper.selectSynchronizeTableData(synchronizetable,keywords,proofreader,status,acquisitionTimeStart,acquisitionTimeEnd,proofreadingTimeStart,proofreadingTimeEnd,lsn,recordsPerPage,user);
        total=unstructureDataMapper.selectSynchronizeTableDataNumber(synchronizetable,keywords,proofreader,status,acquisitionTimeStart,acquisitionTimeEnd,proofreadingTimeStart,proofreadingTimeEnd,user);
        if(synchronizetablelist==null || synchronizetablelist.size() ==0){
            log.info("未查询到同步表数据！");
            return QueryResponse.genErr("未查询到数据!");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        //传给前端需要的id、updateTime、title、dataSource、isproof、proofTime和proofreader
        for(Map<String,Object> m:synchronizetablelist){
            Map<String,Object> map=new HashMap<>();
            map.put("id",m.get("id"));
            if (m.get("update_time")==null||m.get("update_time").equals("")){
                map.put("updateTime","");
            }else {
                map.put("updateTime",m.get("update_time"));
            }
            if (m.get("title")==null||m.get("title").equals("")){
                map.put("title","");
            }else {
                map.put("title",m.get("title"));
            }
            if (m.get("isproof")==null||m.get("isproof").equals("")){
                map.put("isproof","");
            }else{
                map.put("isproof",m.get("isproof"));
            }
            if (m.get("proof_time")==null||m.get("proof_time").equals("")){
                map.put("proofTime","");
            }else {
                map.put("proofTime",m.get("proof_time"));
            }
            if (m.get("proofreader")==null||m.get("proofreader").equals("")){
                map.put("proofreader","");
            }else {
                map.put("proofreader",m.get("proofreader"));
            }
            list.add(map);
        }
        jsonObject.put("list", list);
        int allpage;
        int add=total%recordsPerPage;
        if (add==0){
            allpage=total/recordsPerPage;
        }else {
            allpage=total/recordsPerPage+1;
        }
        jsonObject.put("total", total);
        jsonObject.put("allpage", allpage);
        log.info("查询同步表数据成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询同步表数据成功！");
        return response;
    }

    /**
     * 根据id删除同步表中数据
     * @param datasourcetype 数据源类型
     * @param id ID
     * @param ids ID列表
     * @return QueryResponse
     */
   public QueryResponse deleteDatasource(int datasourcetype, int id, List<Integer> ids){
        QueryResponse response = new QueryResponse();
        if(datasourcetype==1){
            //根据ID 查数据源
            UnstructureData unstructureData = unstructureDataMapper.selectUnstructDataDatasourceById(id);
            if (unstructureData==null){
                log.info("未查到该ID对应数据源！");
                return QueryResponse.genErr("未查到数据源!");
            }
            String synchronizetable=unstructureData.getSyncTable();
            for(Integer i:ids){
                int isOk=unstructureDataMapper.updateSynchronizeTableIsdeleteById(synchronizetable,i);
                if(isOk==0){
                    log.info("根据ID更新同步表isdelete失败！");
                    return QueryResponse.genErr("删除失败!");
                }
                log.info("根据ID更新同步表id为\"+i+\"的数据isdelete成功！");
                response.setSuccess(true);
            }
            response.setMsg("删除数据成功！");
        }else {
            return QueryResponse.genErr("暂无数据!");
        }
        return response;
   }

    /**
     * 保存三元组
     * @param datasourceid 数据源ID
     * @param tripleFile 文本
     * @param subject 主语
     * @param predicate 谓语
     * @param object 宾语
     * @param unit 单位
     * @param predicateType 谓语类型
     * @param proofReader 校对人
     * @return QueryResponse
     */
   public QueryResponse saveTriplet(Integer id, String datasourceid, String tripleFile, String subject, Integer subjectAmount,String predicate, String object, Integer objectAmount,String unit,int predicateType, String proofReader){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        subject=subject.trim();
        predicate=predicate.trim();
        object=object.trim();
        Verify verify = new Verify();
        verify.setId(id);
        verify.setDatasourceid(datasourceid);
        verify.setTripleFile(tripleFile);
        verify.setSubject(subject);
        verify.setSubjectAmount(subjectAmount);
        verify.setPredicate(predicate);
        verify.setObject(object);
        verify.setObjectAmount(objectAmount);
        verify.setUnit(unit);
        verify.setPredicateType(predicateType);
        verify.setProofReader(proofReader);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));//手动设置jvm时间：将时间改为第8时区的时间
        verify.setProofTime(new Timestamp(System.currentTimeMillis()));//更新标注时间为当前时间
       //更新同步表中的该页面实体标注时间
       if (datasourceid.equals("")){
           log.info("数据源ID为空！");
           return QueryResponse.genErr("数据源ID为空!");
       }
       //123
       //分割数据源ID为数据源表中的数据源ID和同步表中每条数据源的ID
       String unstructid=datasourceid.substring(0, datasourceid.indexOf("_"));
       int index = datasourceid.indexOf("_");
       String dataid=datasourceid.substring(index+1);
       //通过数据源表中的数据源ID找到该条数据源的同步表名
       UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(Integer.valueOf(unstructid));
       if (unstructureData==null){
           log.info("未查到该ID对应数据源！");
           return QueryResponse.genErr("未查到数据源!");
       }
       String syncTable= unstructureData.getSyncTable();
       //添加数据来源及url
       verify.setData_source(unstructureDataMapper.getDataNameById(unstructid));
       verify.setUrl(unstructureDataMapper.getUrlById(syncTable,dataid));
       //得到当前时间戳
       Timestamp proofTime = new Timestamp(System.currentTimeMillis());
       //更新同步表中的此数据校对信息
       int isOk=unstructureDataMapper.updateSynchronizeTableTimeById(syncTable,Integer.valueOf(dataid), proofTime);
       if (isOk==0){
           log.info("根据ID更新同步表标注时间失败！");
           return QueryResponse.genErr("更新同步表标注时间失败!");
       }
        //根据ID是否为空判断需要添加还是删除
        if (id==null){//如果传过来的id为空则进行添加
            int flag=insertTriplet(verify);
            if (flag==2){
                log.info("目标三元组已存在！");
                return QueryResponse.genErr("目标三元组已存在!");
            }else if(flag==0){
                log.info("插入三元组失败！");
                return QueryResponse.genErr("插入三元组失败!");
            }else {
                log.info("插入三元组成功！");
                jsonObject.put("id", verify.getId());
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
                response.setSuccess(true);
                response.setMsg("插入三元组成功！");
            }
        }else {//如果传过来的id不为空则进行修改或删除
            int flag=updateTriplet(verify);
            if (flag==-1){//删除
                log.info("修改为空值");
                int ok = verifyMapper.deleteTriplet(id);
                if (ok == 0){
                    response.setSuccess(false);
                    log.info("删除三元组失败！");
                }
                log.info("删除三元组成功！");
                jsonObject.put("id", "");
                response.setSuccess(true);
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
            }else if(flag==2){
                log.info("该三元组不存在！");
                return QueryResponse.genErr("该三元组不存在!");
            }else if(flag==0){
                log.info("更新三元组失败！");
                return QueryResponse.genErr("更新三元组失败!");
            }else {
                log.info("更新三元组成功！");
                jsonObject.put("id", verify.getId());
                response.setSuccess(true);
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
                response.setMsg("更新三元组成功！");
            }
        }
        return response;
   }

    /**
     *保存三元组列表
     * @param verifyList verifyList
     * @return  QueryResponse
     */
    public QueryResponse saveAllTriplet(List<Verify> verifyList,String proofReader){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<Integer> insertIds=new ArrayList<>();//存放新建三元组的id
        List<Integer> deleteIds=new ArrayList<>();//存放所有置为空的三元组ID
       for(Verify verify:verifyList){
           verify.setProofReader(proofReader);
           //根据datasourceid找数据源
           String datasourceid = verify.getDatasourceid();
           //数据源id+页面id
           String unstructid=datasourceid.substring(0, datasourceid.indexOf("_"));
           int index = datasourceid.indexOf("_");
           String dataid=datasourceid.substring(index+1);
           verify.setData_source(unstructureDataMapper.getDataNameById(unstructid));
           verify.setUrl(unstructureDataMapper.getUrlById(unstructureDataMapper.getSyncTable(Integer.valueOf(unstructid)),dataid));
           Timestamp proofTime = new Timestamp(System.currentTimeMillis());
           verify.setProofTime(proofTime);
           //根据ID是否为空判断需要添加还是删除
           if (verify.getId()==null){//如果传过来的id为空则进行添加
               int flag=insertTriplet(verify);
               if (flag==2){
                   log.info("目标三元组已存在！");
                   return QueryResponse.genErr("目标三元组已存在!");
               }else if(flag==0){
                   log.info("插入三元组失败！");
                   return QueryResponse.genErr("插入三元组失败!");
               }else {
                   log.info("插入三元组成功！");
               }
           }else {//如果传过来的id不为空则进行修改或删除
               int flag=updateTriplet(verify);
               if (flag==-1){
                   log.info("修改为空值");
                   deleteIds.add(verify.getId());
               }else if(flag==2){
                   log.info("该三元组不存在！");
                   return QueryResponse.genErr("该三元组不存在!");
               }else if(flag==0){
                   log.info("更新三元组失败！");
                   return QueryResponse.genErr("更新三元组失败!");
               }else {
                   log.info("更新三元组成功！");
               }
           }
           insertIds.add(verify.getId());
       }
//       //添加三元组后返回所有三元组的ID
//       if (!insertIds.isEmpty()){
//           jsonObject.put("ids", insertIds);
//       }

       //删除所有置空的三元组
        if (!deleteIds.isEmpty()){
            for(Integer id:deleteIds){
                int isOk = verifyMapper.deleteTriplet(id);
                if (isOk == 0){
                    response.setSuccess(false);
                    log.info("删除三元组失败！");
                }
                log.info("删除三元组成功！");
            }
        }else {
            //新建的修改后都将三元组的ID传给前端
            jsonObject.put("ids", insertIds);
        }
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setSuccess(true);
        response.setMsg("插入三元组成功！");
        return response;
    }




    /**
     *插入一个三元组
     * @param verify  verify
     * @return 插入结果：2：三元组已存在；1：插入成功；0：插入失败
     */
    int insertTriplet(Verify verify){
        //TODO
        //查看是否已经存在的时候，是否判断datasource一致？？？？？？
        int tag=0;
        //若是实体类型三元组，只判断主、谓语、页面来源是否相同，如果是关系三元组，则判断主谓宾和来源是否一致
        if(verify.getPredicateType()==0){
            tag=verifyMapper.checkTypeTriple(verify);//查看类型三元组是否已经存在,不存再则进行插入
        }else {
            tag=verifyMapper.checkTriple(verify); //查看三元组是否已经存在,不存再则进行插入
        }
        if(tag>0){
            log.info("该数据源下目标三元组已存在！");
            return 2;//三元组已存在
        }
        int isOk=verifyMapper.insertTriplet(verify);
        if(isOk==0){
            log.info("插入三元组失败！");
            return 0;//插入三元组失败
        }
        return 1;//插入三元组成功
    }

    /**
     * 修改一个三元组
     * @param verify  verify
     * @return 更新结果：-1：删除三元组的值 2：三元组不存在；1：更新成功；0：更新失败
     */
    int updateTriplet(Verify verify){
        String o = verify.getObject().toString();
        log.info(o);
        if(verify.getSubject().trim().equals("") || verify.getSubject()==null || verify.getObject().trim().equals("")||verify.getObject()==null){
            return -1;//删除该三元组的值
        }
        Verify v=verifyMapper.selectByPrimaryKey(verify.getId()); //查看三元组是否存在，存在再进行修改
        if(v==null){
            log.info("该三元组不存在！");
            return 2;//三元组不存在
        }
        //判断是否为实体名修改，如果为实体名修改
        if (v.getPredicateType()==0){
            //找到原实体名，即原三元组的头实体
            String oldEntityName=   v.getSubject();
            String datasourceId= v.getDatasourceid();//数据源ID
            //找到库中所有使用原实体名作为头实体的三元组并更新
            List<Verify> headVerifylist=verifyMapper.queryEntityByHeadname(oldEntityName,datasourceId);
            if(null == headVerifylist || headVerifylist.size() ==0 ){
                log.info("查询到头实体为该实体名的实体");
            }
            for(Verify ver:headVerifylist){
                ver.setSubject(verify.getSubject());
                int isOk=verifyMapper.updateTriplet(ver);
                if(isOk==0){
                    log.info("更新头实体为该实体名的三元组失败！");
                    return 0;//更新失败
                }
            }
            //找到库中所有使用原实体名作为尾实体的三元组并更新
            List<Verify> tailVerifylist=verifyMapper.queryEntityByTailname(oldEntityName,datasourceId);
            if(null == tailVerifylist || tailVerifylist.size() ==0 ){
                log.info("查询到尾实体为该实体名的实体");
            }
            for(Verify ver:tailVerifylist){
                ver.setObject(verify.getSubject());
                int isOk=verifyMapper.updateTriplet(ver);
                if(isOk==0){
                    log.info("更新尾实体为该实体名的三元组失败！");
                    return 0;//更新失败
                }
            }
        }
        //修改该三元组本身
        int isOk=verifyMapper.updateTriplet(verify);
        if(isOk==0){
            log.info("更新三元组失败！");
            return 0;
        }
        log.info("更新三元组成功！");
        return 1;
    }




    //TODO
    /**
     * 搜索实体别名（搜索范围为库中实体名和实体别名）
     * @param entityName 实体名
     * @return QueryResponse
     */
    public QueryResponse queryAliasName(String entityName){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        if (!entityName.equals("")){
            entityName="%"+entityName+"%";
        }
        Set<String> aliasNameSet = new HashSet<>();
        //模糊检索实体名为entityName的实体
        List<Verify>  verifyList1= verifyMapper.fuzzyQueryEntityname(entityName);
        for(Verify verify:verifyList1){
            aliasNameSet.add(verify.getSubject());
        }
        //模糊检索实体别名为entityName的实体
        List<Verify>  verifyList2= verifyMapper.fuzzyQueryEntityAlias(entityName);
        for(Verify verify:verifyList2){
            //将实体别名字符串转为列表
            List<String> aliasNames=Arrays.asList(verify.getObject().split("@#@"));
            //将列表中的别名分别加入aliasNameSet
            entityName = entityName.replace("%","");
            for(String alias:aliasNames){
                if (alias.contains(entityName)){
                    aliasNameSet.add(alias);
                }
            }
        }
        if (aliasNameSet.size()==0){
            log.info("未检索出实体别名！");
            return QueryResponse.genErr("未检索出实体别名!");
        }
        jsonObject.put("aliasName", aliasNameSet);
        log.info("检索实体别名成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("检索实体别名成功！");
        return response;
    }


    /**
     * 删除三元组
     * @param id 三元组ID
     * @return QueryResponse
     */
   public QueryResponse deleteTri(int id){
        QueryResponse response = new QueryResponse();
        int isOk = verifyMapper.deleteTriplet(id);
        if (isOk == 0){
            response.setSuccess(false);
            log.info("删除三元组失败！");
            return QueryResponse.genErr("删除三元组失败!");
        }
        log.info("删除三元组成功！");
        response.setSuccess(true);
        response.setMsg("删除成功！");
        return response;
   }

    public boolean deleteTriplet(int id){
        QueryResponse response = new QueryResponse();
        int isOk = verifyMapper.deleteTriplet(id);
        if (isOk == 0){
            response.setSuccess(false);
            log.info("删除三元组失败！");
            return false;
        }
        log.info("删除三元组成功！");
        return true;
    }


    /**
     * 校对完成、审核完成
     * @param datasourceid 数据源ID
     * @param status 数据状态 0：未标注； 1：已标注；2：已通过；3：已打回；4：已返工
     * @return QueryResponse
     */
   public QueryResponse proofreadingCompleted(String datasourceid,int status){
        QueryResponse response = new QueryResponse();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));//手动设置jvm时间：将时间改为第8时区的时间
        if (datasourceid.equals("")){
            log.info("数据源ID为空！");
            return QueryResponse.genErr("数据源ID为空!");
        }
        String reviewername = userMapper.findManagerById(String.valueOf(TokenUtils.getTokenUserId()));
        //分割数据源ID为数据源表中的数据源ID和同步表中每条数据源的ID
        String unstructid=datasourceid.substring(0, datasourceid.indexOf("_"));
        int index = datasourceid.indexOf("_");
        String dataid=datasourceid.substring(index+1);
        //通过数据源表中的数据源ID找到该条数据源的同步表名
        UnstructureData unstructureData=unstructureDataMapper.selectUnstructDataDatasourceById(Integer.valueOf(unstructid));
        if (unstructureData==null){
            log.info("未查到该ID对应数据源！");
            return QueryResponse.genErr("未查到数据源!");
        }
        String syncTable= unstructureData.getSyncTable();
        if(status ==1){ //得到当前时间戳
            Timestamp proofTime = new Timestamp(System.currentTimeMillis());
            //更新同步表中的此数据校对信息
            int isOk=unstructureDataMapper.updateSynchronizeTableStatusIsproofAndIsreworkById(syncTable,Integer.valueOf(dataid), proofTime, status);
            if (isOk==0){
                log.info("根据ID更新同步表isproof失败！");
                return QueryResponse.genErr("失败!");
            }
            log.info("更新id为"+datasourceid+"同步表isproof成功！");
            response.setSuccess(true);
            response.setMsg("校对完成");
        }
        else if (status ==2){
            //判断是否存在打回的三元组
            List<Verify> list = verifyMapper.getAllEntity(datasourceid);
            int isPass =0;
            for(Verify li : list){
                isPass += verifyMapper.selectTheWholeEntityStatus(li.getSubject(),datasourceid);
            }
            if(isPass !=0){
                response.setSuccess(false);
                response.setMsg("存在错误三元组！不能通过！请重新审核！");
                return response;
            }
            Timestamp reviewTime = new Timestamp(System.currentTimeMillis());
            //更新同步表中的此数据校对信息
            int isOk=unstructureDataMapper.updateSynchronizeTableStatusIsreviewAndIscallbackById(syncTable,Integer.valueOf(dataid),reviewTime,reviewername,status);
            if (isOk==0){
                log.info("根据ID更新同步表isreview失败！");
                return QueryResponse.genErr("失败!");
            }
            log.info("更新id为"+datasourceid+"同步表isreview成功！");
            response.setSuccess(true);
            response.setMsg("审核通过完成");
        }
        else if(status == 3){
            //判断是否存在错误三元组
            List<Verify> list = verifyMapper.getAllEntity(datasourceid);
            int isPass =0;
            for(Verify li : list){
                isPass += verifyMapper.selectTheWholeEntityStatus(li.getSubject(),datasourceid);
            }
            if(isPass ==0){
                response.setSuccess(false);
                response.setMsg("不存在错误三元组！不能打回！请重新审核！");
                return response;
            }

            Timestamp reviewTime = new Timestamp(System.currentTimeMillis());
            //更新同步表中的此数据校对信息
            int isOk=unstructureDataMapper.updateSynchronizeTableStatusIsreviewAndIscallbackById(syncTable,Integer.valueOf(dataid),reviewTime,reviewername,status);
            if (isOk==0){
                log.info("根据ID更新同步表iscallback失败！");
                return QueryResponse.genErr("失败!");
            }
            log.info("更新id为"+datasourceid+"同步表iscallback成功！");
            response.setSuccess(true);
            response.setMsg("审核打回完成");
        }
        else if (status == 4){
           Timestamp proofTime = new Timestamp(System.currentTimeMillis());
           //更新同步表中的此数据校对信息
           int isOk=unstructureDataMapper.updateSynchronizeTableStatusIsproofAndIsreworkById(syncTable,Integer.valueOf(dataid), proofTime,status);
           if (isOk==0){
               log.info("根据ID更新同步表isrework失败！");
               return QueryResponse.genErr("失败!");
           }
           log.info("更新id为"+datasourceid+"同步表isrework成功！");
           response.setSuccess(true);
           response.setMsg("返工完成");
       }
        return response;
   }


    /**
     * 查询所有子类
     * @return QueryResponse
     */
   public QueryResponse CheckSubcategoriesByMajorCategories(){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
       List<Map<String,String>> allLeaf= metadataService.getAllLeafNodeName();
        if (allLeaf.isEmpty()){
            log.info("未查到所有子类");
            return QueryResponse.genErr("未查到子类!");
        }
       List<Map<String,String>> allLeafList=new ArrayList<>();
       for (Map<String,String> entType:allLeaf) {
           Map<String,String> allLeafMap=new HashMap<>();
           allLeafMap.put("id",entType.get("id"));
           allLeafMap.put("name",entType.get("name"));
           allLeafList.add(allLeafMap);
       }
        jsonObject.put("allLeafList", allLeafList);
        log.info("查询所有子类成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询所有子类成功！");
        return response;
   }

    /**
     *查询实体类型
     * @param datasourceid 数据源ID
     * @return QueryResponse
     */
    public QueryResponse checkEntityType(String datasourceid){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<String> entityTypeList=verifyMapper.checkEntityType(datasourceid);
        Set<String> entityTypeSet = new HashSet<>();
        if(null == entityTypeList || entityTypeList.size() ==0 ){
            log.info("未查询到该数据源下已校对的实体类型！");
            return QueryResponse.genErr("\uD83D\uDE00 \uD83E\uDD17 \uD83D\uDE1C");
        }
        entityTypeSet.addAll(entityTypeList);
        jsonObject.put("entityTypeList", entityTypeSet);
        log.info("查询实体类型成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询实体类型成功！");
        return response;
    }

    /**
     *查询实体属性和对应值
     * @param categoryId 类别ID
     * @param entityName 实体名
     * @return QueryResponse
     */
    public QueryResponse checkAttributeAndValue(String categoryId,String entityName,String datasourceId){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        //查找此类实体下的对应属性
        List<PropertyType> propertiesList = metadataService.getProperties(categoryId);//调用获取数据源中属性和单位的函数
        if(propertiesList.isEmpty()){
            log.info("未查询到类型为"+categoryId+"属性！");
            return QueryResponse.genErr("未查询到类型为"+categoryId+"属性！");
        }
        //在数据库中查该实体的属性值
        List<Verify> verifylist=verifyMapper.selectAttributeTripleByPredicateType(entityName,datasourceId);
        List<Map<String,String>> attributeList=new ArrayList<>();
        for (PropertyType proType:propertiesList){//对源数据中的属性进行遍历
            Map<String,String> attributeMap=new HashMap<>();
            String name;
//            if(StringUtils.isEmpty(proType.getQualifier())){
//                attributeMap.put("qualifier", "");
//                name = proType.getName();
//            }else{
//                attributeMap.put("qualifier", proType.getQualifier());
//                //TODO 这里的分隔符应该写成一个常量
//                name = proType.getQualifier()+"#"+proType.getName();
//            }
            name = proType.getName();
            attributeMap.put("name", proType.getName());
           // attributeMap.put("status",proType.getStatus());
//            if(StringUtils.isEmpty(proType.getQualifier())){
//                attributeMap.put("qualifier", "");
//            }else{
//                attributeMap.put("qualifier", proType.getQualifier());
//            }///
            attributeMap.put("qualifier", "");
            attributeMap.put("allowRangeValue", String.valueOf(proType.getAllowRangeValue()));
            if(proType.getDefaultUnit()!=null){
                attributeMap.put("defaultUnit", proType.getDefaultUnit());
            }else {
                attributeMap.put("defaultUnit", "");
            }
            List<String> unitList=proType.getUnit();
            if (!unitList.isEmpty()&& unitList!=null){
                StringJoiner stringJoiner = new StringJoiner(",");
                for(String unit: unitList){
                    stringJoiner.add(unit);
                }
                if(StringUtils.isEmpty(stringJoiner.toString())){
                    attributeMap.put("unit", "");
                }else{
                    attributeMap.put("unit", stringJoiner.toString());
                }
            }else{
                attributeMap.put("unit", "");
            }
            if(proType.getDataType()!=null){
                attributeMap.put("valueType", proType.getDataType().toString());
            }else {
                attributeMap.put("valueType", "");
            }

            //下面开始查找是否已经有标记完成的属性值，并将其添加到返回结果中。
            if(verifylist!=null&&!verifylist.isEmpty()){
                List<Verify> attrs = verifylist.stream().filter(att-> name.equals(att.getPredicate())).collect(Collectors.toList());
                if(!attrs.isEmpty()){
                    //TODO 属性值可以有多个，但目前暂时取第一个
                    //找到了匹配的已经标注的数据值
                    attributeMap.put("attrValue",attrs.get(0).getObject());
                    attributeMap.put("tripleFile",attrs.get(0).getTripleFile());
                    attributeMap.put("id",String.valueOf(attrs.get(0).getId()));
                    attributeMap.put("status", String.valueOf(attrs.get(0).getStatus()));
                    attributeMap.put("unitValue",attrs.get(0).getUnit());
                }
            }else{
                attributeMap.put("attrValue","");
                attributeMap.put("tripleFile","");
                attributeMap.put("id","");
                attributeMap.put("status", "");
                attributeMap.put("unitValue","");

            }
            attributeList.add(attributeMap);
        }
        //查找该实体类型三元组
        jsonObject.put("attributeList", attributeList);
        //根据实体名和页面ID检索实体类型三元组
        //根据类型ID找到实体名
        String categoryName =  metadataService.getEntityNameById(categoryId);
        Verify verify = verifyMapper.getEntityByNameAndCategory(datasourceId,entityName,categoryName);
        if (verify!=null){
            jsonObject.put("entityType", verify);
        }else {
            jsonObject.put("entityType", "");
        }
        log.info("查询类型为"+categoryId+"属性成功！");
        response.setSuccess(true);
        response.setMsg("查询类型为"+categoryId+"属性成功！");
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        return response;
    }

    /**
     * 查询某类下实体的所有关系
     * @param categoryId 类别ID
     * @return QueryResponse
     */
    public QueryResponse CheckRelation(String categoryId){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();

        List<RelationType> domainrelation = metadataService.getDomainRelations(categoryId);//调用获取domain关系
        List<RelationType> rangerelation = metadataService.getRangeRelations(categoryId);//调用获取range关系
        List<String> list=new ArrayList<>();
        for (RelationType relType:domainrelation){
            list.add(relType.getName());
        }
        for(RelationType relType:rangerelation){
            list.add(relType.getName());
        }
        //去重（比如舰载船既是舰艇的domain关系，又是range关系）
        List<String> relationlist = new ArrayList<>(new LinkedHashSet<>(list));
        jsonObject.put("relationlist",relationlist);
        log.info("查询类型为"+categoryId+"关系成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询类型为"+categoryId+"关系成功！");
        return response;
    }

    /**
     * 查询某类下实体的所有兄弟
     * @param categoryId 类别ID
     * @return QueryResponse
     */
    public QueryResponse checkBrothers(String categoryId){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        Map<String,String> brotherMap = new HashMap<>();
        List<Map<String, String>>  brothers = metadataService.getBrothers(categoryId);//调用获取兄弟节点
        for (Map<String, String> entType:brothers){
            brotherMap.put(entType.get("id"),entType.get("name"));
        }
        //VoEntType category = getNameById(categoryId);
        String category = metadataService.getEntityNameById(categoryId);
        if (category==null){
            log.info("根据ID查找实体类型名为空！");
        }else {
            brotherMap.put(categoryId,category);
        }
        jsonObject.put("brotherlist", brotherMap);
        log.info("查询所有兄弟节点成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询兄弟节点成功！");
        return response;
    }

    /**
     * 查询该实体关系三元组
     * @param entityName 实体名
     * @return QueryResponse
     */
    public  QueryResponse checkRelationAndValue(String entityName,String datasourceId){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<Verify> verifylist=verifyMapper.selectRelationTripleByPredicateType(entityName,datasourceId);
        if(null == verifylist || verifylist.size() ==0 ){
            log.info("根据实体名查询未到关系三元组");
           // return QueryResponse.genErr("根据实体名查询未到关系三元组!");
        }
        List<Map<String,String>> entityRelationlist=new ArrayList<>();
        for(Verify v:verifylist){
            Map<String,String> entityRelationMap=new HashMap<>();
            entityRelationMap.put("id",v.getId().toString());
            entityRelationMap.put("subject",v.getSubject());
            entityRelationMap.put("predicate",v.getPredicate());
            entityRelationMap.put("object",v.getObject());
            //加入主语和谓语数量,变成5元组
            entityRelationMap.put("subjectAmount",v.getSubjectAmount().toString());
            entityRelationMap.put("objectAmount",v.getObjectAmount().toString());
            entityRelationMap.put("tripleFile",v.getTripleFile());
            entityRelationMap.put("status",StringUtil.changeFormat(v.getStatus()));
            //TODO
            //是否审核加入
//            entityRelationMap.put("ispass",v.getIspass().toString());
            entityRelationlist.add(entityRelationMap);
        }
        jsonObject.put("entityRelationlist", entityRelationlist);
        //根据实体名和页面ID检索实体类型三元组
        Verify verify = verifyMapper.getEntityByNameAndDatasourceId(datasourceId,entityName);
        if (verify!=null){
            jsonObject.put("entityType", verify);
        }else {
            jsonObject.put("entityType", "");
        }
        log.info("根据实体名查询关系成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("根据实体名查询关系成功！");
        return response;
    }

    /**
     * 根据实体类型查询实体名
     * @param entitytype 实体名
     * @return QueryResponse
     */
    public QueryResponse queryEntityNameByEntityType(String entitytype){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<Verify> verifylist=verifyMapper.selectEntityByEntityType(entitytype);
        Set<String> entitynamelist=new HashSet<>();
        if(null == verifylist || verifylist.size() ==0 ){
            log.info("根据实体类型未查询到实体");
            return QueryResponse.genErr("\uD83D\uDE1C");
        }
        for(Verify v:verifylist){
            entitynamelist.add(v.getSubject());
        }
        if(entitynamelist.size() ==0 ){
            log.info("没有该找到该实体！");
            return QueryResponse.genErr("没有该找到该实体!");
        }
        jsonObject.put("entitynamelist", entitynamelist);
        log.info("根据实体类型查询实体成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("根据实体类型查询实体成功！");
        return response;
    }

    /**
     * 选定关系名后返回所有可以选择的实体名
     * @param relation 关系
     * @param categoryId 类别ID
     * @return QueryResponse
     */
    public QueryResponse queryentityByRelation(String relation, String categoryId){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String category=null;
        String type = metadataService.getEntityNameById(categoryId);
        if (type==null){
            log.info("根据ID检索类型名失败");
        }else {
            category=type;
        }
       // String entitytype="";
        List<String> entitytypeList = new ArrayList<>();//由于关系可能拥有不同的头尾实体，例如导弹装备的头实体可能是战机、舰艇、对抗系统
        List<RelationType> domainrelation = metadataService.getDomainRelations(categoryId);//调用获取domain关系
        List<RelationType> rangerelation = metadataService.getRangeRelations(categoryId);//调用获取range关系
        //将domain关系和range关系放在一个list里
        List<RelationType> relations=new ArrayList<>();
        relations.addAll(domainrelation);
        relations.addAll(rangerelation);
        for(RelationType proType:relations){
            if (proType.getName().equals(relation)){//如果关系列表中的某个关系名等于传入的关系名
                //得到关系中的尾实体名并赋值给tailEntity
                String tailEntity = proType.getRange();
                //得到关系中的头实体名并赋值给headEntity
                String headEntity = proType.getDomain();
                //得到尾实体类型对应的所有叶子，如果尾实体所有叶子中包括传入的类别，则检索库中所有类型为头实体的实体名，反之检索类型为尾实体的实体名
                String tailEntityValue="";
                List<Map<String, String>> descendList=metadataService.getAllSubclass(tailEntity);
                if (descendList.isEmpty()){
                    tailEntityValue=tailEntity;
                }else{
                    for (Map<String, String> entType : descendList) {
                        if (entType.get("name").equals(category)) {
                            tailEntityValue=entType.get("name");
                            break;
                        }
                    }
                }
                if (tailEntityValue.equals(category)){
                    //entitytype=headEntity;\
                    entitytypeList.add(headEntity);
                }else {
                   // entitytype=tailEntity;
                    entitytypeList.add(tailEntity);
                }
            }
        }
        Set<String>  entitynameSet=new HashSet<>();
        for(String entitytype:entitytypeList){
            //查询该实体类型下的所有叶子节点
            //  List<VoEntType> leaflist=getDescendants(entitytype);
            List<Map<String, String>> leaflist=metadataService.getAllSubclass(entitytype);
            if (leaflist.isEmpty()){//如果已经是叶子节点，则直接在库中查询该叶子对应的的实体名
                log.info("未查询到该实体叶子结点");
                List<Verify> verifylist=verifyMapper.selectEntityByEntityType(entitytype);
                for (Verify v:verifylist){
                    entitynameSet.add(v.getSubject());
                }
            }else {//如果不是叶子节点，则找到该类型下所有叶子后在库中查询叶子对应的的实体名
                List<String> allLeafList = new ArrayList<>();
                for(Map<String, String> entType:leaflist){
                    allLeafList.add(entType.get("name"));
                }
                for(String leaf:allLeafList){
                    List<Verify> verifylist=verifyMapper.selectEntityByEntityType(leaf);
                    for (Verify v:verifylist){
                        entitynameSet.add(v.getSubject());
                    }
                }
            }
        }
        if (entitynameSet.size()==0){ //如果库中没有该类叶子对应的实体名则进行提示
            jsonObject.put("entitynamelist", "");
            response.setMsg("未找到该类型对应实体，请去添加实体处进行添加！");
            log.info("未找到该类型对应实体！");
        }else {
            jsonObject.put("entitynamelist", entitynameSet);
            response.setMsg("选定关系名后返回所有可以选择的实体名成功！");
            log.info("选定关系名后返回所有可以选择的实体名成功！");
        }
        int entityLocation=entityLocation(relation,categoryId);
        jsonObject.put("entityLocation", entityLocation);
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        return response;
    }

    /**
     * 列表页进入实体校对详情页
     * @param datasourceid 数据源ID
     * @param id 页面ID
     * @return QueryResponse
     */
    public QueryResponse getEntityContent(int datasourceid, int id){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        UnstructureData unstructureData = unstructureDataMapper.getSynList(datasourceid);
        String sync_table = unstructureData.getSyncTable();
        Map<String,Object> contentMap = unstructureDataMapper.getEntityContent(id,sync_table);
        int status = unstructureDataMapper.getStatusByDataId(sync_table,id);
        jsonObject.put("update_time", contentMap.get("update_time"));
        jsonObject.put("title", contentMap.get("title"));
        jsonObject.put("content", contentMap.get("content"));
        jsonObject.put("status",status);
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("获取实体详情页内容成功");
        log.info("获取实体详情页内容成功！");
        return response;
    }

    /**
     * 删除包含该实体的所有三元组
     * @param entityName 实体名
     * @return QueryResponse
     */
    public QueryResponse deleteEntity(String entityName){
        QueryResponse response = new QueryResponse();
        int re = verifyMapper.deleteEntity(entityName);
        if(re == 0){
            response.setSuccess(false);
            response.setMsg("删除实体失败");
            log.info("删除 [{}] 失败！", entityName);
        }else {
            response.setSuccess(true);
            response.setMsg("删除三元组成功");
            log.info("删除与{}相关的三元组成功",entityName);
        }

        return response;
    }

    /**
     * 查看实体Html详情页已存在的实体
     * @param datasourceId 数据源ID
     * @return QueryResponse
     */
    public QueryResponse getAllEntity(String datasourceId, String entityType,String entityName) {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<Map<String,String>> allLeaf = metadataService.getAllLeafNodeName();//调用查询所有子类方法getAllLeaf()
        if (allLeaf.isEmpty()){
            log.info("未查到所有子类");
            return QueryResponse.genErr("未查到子类!");
        }
        for (Map<String,String> entType:allLeaf) {
            Map<String,String> allLeafMap=new HashMap<>();
            allLeafMap.put("id",entType.get("id"));
            allLeafMap.put("name",entType.get("name"));
            leafs.add(allLeafMap);
        }
        //得到实体类型ID
        String entityTypeId=null;
        for (Map<String,String> leafMap:leafs){
            if (leafMap.get("name").equals(entityType)){
                entityTypeId=leafMap.get("id");
            }
        }
        //TODO
        if(entityType.equals("")&&entityName.equals("")){//如果实体类型和实体名称都为空，则默认检索该页面所有实体
            List<Verify> list = verifyMapper.getAllEntity(datasourceId);
            String allEntityTypeId=null;
            List<Map<String,String>> entityTypelist=new ArrayList<>();
            for(Verify li : list){
                String allEntityType = verifyMapper.getEntityType(li.getSubject(),datasourceId);
                for (Map<String,String> leafMap:leafs){
                    if (leafMap.get("name").equals(allEntityType)){
                        allEntityTypeId=leafMap.get("id");
                    }
                }
                int isOk = verifyMapper.selectTheWholeEntityStatus(li.getSubject(),datasourceId);
                HashMap map = new HashMap();
                map.put("entity",li.getSubject());
                map.put("entityType", allEntityType);
                map.put("entityTypeId",allEntityTypeId);
                map.put("entityTypeStatus",StringUtil.changeFormat(li.getStatus()));
                map.put("isPass",isOk);
                entityTypelist.add(map);
            }
            jsonObject.put("entityNum", list.size());
            jsonObject.put("entity", entityTypelist);
            log.info("名称内容都为空检索实体成功！！");
        }else if(entityType.equals("")&&!entityName.equals("")) {//类型为空,名字不等于空,根据名称检索实体
            if (!entityName.equals("")){
                entityName="%"+entityName+"%";
            }
            List<Verify> newList =verifyMapper.getEntityByName(datasourceId,entityName);
            List<Map<String,String>> newEntityTypelist=new ArrayList<>();
            for(Verify newLi : newList){
                HashMap newMap = new HashMap();
                newMap.put("entity",newLi.getSubject());
                String type = verifyMapper.getEntityType(newLi.getSubject(),datasourceId);
                String typeId="";
                for (Map<String,String> leafMap:leafs){
                    if (leafMap.get("name").equals(type)){
                    typeId=leafMap.get("id");
                    }
                }
                int isOk = verifyMapper.selectTheWholeEntityStatus(newLi.getSubject(),datasourceId);
                newMap.put("entityType", type);
                newMap.put("entityTypeId",typeId);
                newMap.put("entityTypeStatus",StringUtil.changeFormat(newLi.getStatus()));
                newMap.put("isPass",isOk);
                newEntityTypelist.add(newMap);
            }
            jsonObject.put("entityNum", newList.size());
            jsonObject.put("entity", newEntityTypelist);
            log.info("类型为空，根据该名称检索实体成功！！");
        }else if(!entityType.equals("")&&entityName.equals("")){//类型不为空，名字为空，，则检索该类型下所有实体
            List<Verify> newList = verifyMapper.getEntityByType(datasourceId, entityType);
            List<Map<String,String>> newEntityTypelist=new ArrayList<>();
            for(Verify newLi : newList){
                HashMap newMap = new HashMap();
                int isOk = verifyMapper.selectTheWholeEntityStatus(newLi.getSubject(),datasourceId);
                newMap.put("entity",newLi.getSubject());
                newMap.put("entityType", entityType);
                newMap.put("entityTypeId",entityTypeId);
                newMap.put("entityTypeStatus",StringUtil.changeFormat(newLi.getStatus()));
                newMap.put("isPass",isOk);
                newEntityTypelist.add(newMap);
            }
            jsonObject.put("entityNum", newList.size());
            jsonObject.put("entity", newEntityTypelist);
            log.info("类型不为空，名字为空，根据类型检索实体成功！！");
        } else{//类型名字都不为空
            if (!entityName.equals("")){
                entityName="%"+entityName+"%";
            }
            List<Verify> newList = verifyMapper.getEntity(datasourceId, entityType,entityName);
            List<Map<String,String>> newEntityTypelist=new ArrayList<>();
            for(Verify newLi : newList){
                HashMap newMap = new HashMap();
                int isOk = verifyMapper.selectTheWholeEntityStatus(newLi.getSubject(),datasourceId);
                newMap.put("entity",newLi.getSubject());
                newMap.put("entityType", entityType);
                newMap.put("entityTypeId",entityTypeId);
                newMap.put("entityTypeStatus",StringUtil.changeFormat(newLi.getStatus()));
                newMap.put("isPass",isOk);
                newEntityTypelist.add(newMap);
            }
            jsonObject.put("entityNum", newList.size());
            jsonObject.put("entity", newEntityTypelist);
            log.info("类型不为空，名字为空，根据类型检索实体成功！！");
        }
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查看实体Html详情页已存在的实体成功");
        log.info("查看实体Html详情页已存在的实体成功！");
        return response;
    }

    /**
     * 保存所有属性
     * @param datasourceId 当前HTML页面的id
     * @param properties 属性名 属性值 text文本
     * @param entityName 实体名
     * @param proofReader 校对人
     * @return QueryResponse
     */
    public QueryResponse saveProperties(String datasourceId, List<List<String>> properties, String entityName, String proofReader){
        QueryResponse response = new QueryResponse();
        for(List<String> list : properties){
            String predicate = list.get(0);
            String object = list.get(1);
            String tripleFile = list.get(2);
            Verify verify = new Verify(datasourceId,tripleFile,entityName,predicate,object,1,proofReader);
            if(!object.equals("")){
                int tag1 = verifyMapper.checkTriple(verify);//查看三元组是否存在
                int tag2 = verifyMapper.checkBinary(entityName, predicate);//查看二元组是否存在
                if(tag2 == 0){
                    int id=verifyMapper.insertTriplet(verify);
                    if(id == 1) {
                        log.info("插入三元组[{}][{}][{}]成功！",entityName,predicate,object);
                    }
                } else if(tag1 == 0 && tag2 > 0){
                    int isOk=verifyMapper.updateTriplet(verify);
                    if(isOk > 0){
                        log.info("更新三元组[{}][{}][{}]成功！",entityName,predicate,object);
                    }
                }
            }
        }
        response.setSuccess(true);
        response.setMsg("保存属性成功");
        log.info("保存属性成功！");
        return response;
    }

    /**
     * 更改实体类型
     * @param entityType 实体类型
     * @param entityName 实体名
     * @param newEntityType 新实体类型
     * @return QueryResponse
     */
    public QueryResponse changeEntityType(String entityType, String entityName, String newEntityType){
        QueryResponse response = new QueryResponse();
        List<Map<String,String>> properties = verifyMapper.getProperties(entityName);
        //List<VoProType> attributeList = getAttributeList(entityType);
        List<PropertyType> attributeList = metadataService.getProperties(entityType);
        List<String> tagList = new ArrayList<>();
        //根据实体类型ID获取实体名
        String newEntityTypeName=metadataService.getEntityNameById(newEntityType);
        int tag=0;
        if (newEntityTypeName!=null){
            tag = verifyMapper.updateObject(entityName, newEntityTypeName);
        }
        if(tag == 0){
            response.setSuccess(false);
            response.setMsg("更改实体类型失败");
            log.info("更改实体类型失败！");
            return response;
        }
        if(properties.isEmpty()){
            response.setSuccess(true);
            response.setMsg("更改实体类型成功");
            log.info("更改实体类型成功！");
            return response;
        }
        for(Map<String, String> pro : properties){
            for(PropertyType li :attributeList){
                if (li.getName().equals(pro.keySet().iterator().next())){
                    tagList.add(pro.keySet().iterator().next());
                }
            }
        }
        for (Map<String, String> key : properties){
            int i =0;
            for (String li:tagList){
                if (key.keySet().iterator().next().equals(li)) {
                    i = 1;
                    break;
                }
            }
            if(i == 0) {
                int re = verifyMapper.delTriplet(entityName, key.keySet().iterator().next());
                if (re == 1) log.info("删除三元组[{}][{}]成功", entityName, key.keySet().iterator().next());
            }
        }
        response.setSuccess(true);
        response.setMsg("更改实体类型成功");
        log.info("更改实体类型成功！");
        return response;
    }


    /**
     * 判断关系中实体位置
     */
    private int entityLocation(String relation,String categoryId){
        int entityLocation=0;
        List<RelationType> domainrelation = metadataService.getDomainRelations(categoryId);//调用获取domain关系
        for(RelationType relType:domainrelation) {
            if (relType.getName().equals(relation)) {
                break;
            }
        }
        List<RelationType> rangerelation = metadataService.getRangeRelations(categoryId);//调用获取range关系
        for(RelationType relType:rangerelation) {
            if (relType.getName().equals(relation)) {
                entityLocation = 1;
                break;
            }
        }
        if (relation.equals("舰载船")){
            entityLocation=0;
        }
        if (relation.contains("前身")){
            entityLocation=0;
        }
        if (relation.contains("次型")){
            entityLocation=0;
        }
        if (relation.contains("前型")){
            entityLocation=0;
        }
        if (relation.contains("同型")){
            entityLocation=0;
        }
        if (relation.contains("同级")){
            entityLocation=0;
        }
        if (relation.contains("舰级")){
            entityLocation=0;
        }
        log.info("判断实体位置成功！");
        return entityLocation;
    }

    ///

}
