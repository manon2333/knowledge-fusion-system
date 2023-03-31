package com.edu.bistu.datacollectproofaudit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.mapper.VerifyMapper;
import com.edu.bistu.datacollectproofaudit.mapper.UnstructureDataMapper;
import com.edu.bistu.datacollectproofaudit.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import com.edu.bistu.datacollectproofaudit.pojo.*;

/*
@author wtt
管理人员管理标注人员的一些功能：标注人员列表页+标注人员详情页
需要的数据项有：
用户名、未标注数据数量、未审核数据数量、已审核数据数量、新上传数据数量?需要设计新上传数据应该放在哪个表里，需要哪些字段
 */
@Service
public class ManageService {
    private final UserMapper userMapper;

    private final UnstructureDataMapper unstructureDataMapper;

    private final VerifyMapper verifyMapper;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public ManageService(@Autowired UserMapper userMapper,
                         @Autowired UnstructureDataMapper unstructureDataMapper,
                         @Autowired VerifyMapper verifyMapper) {
        this.userMapper = userMapper;
        this.unstructureDataMapper = unstructureDataMapper;
        this.verifyMapper = verifyMapper;
    }

    /*
    标注人员总体工作情况列表获取
    @return id  用户id
    @return username  用户名
    @return disproof  未标注数据数量
    @return proof     已标注数据数量
    @return review    已审核数据数量
    @return callback  已打回数据数量
    @return rework    已返工数据数量
    @return newput    新上传数据数量  //TODO
     */
    public QueryResponse getMessage(String namekeyword, Integer pageNumber, Integer displayNumber) {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        int lsn = (pageNumber - 1) * displayNumber;
        int totalnum = 0;
        int allpage = 0;
        int proof = 0;
        int review = 0;
        int disproof = 0;
        int callback = 0;
        int rework = 0;
        int newput = 0;
        if (!namekeyword.equals("") && namekeyword != null) {
            namekeyword = namekeyword.trim();
            namekeyword = "%" + namekeyword + "%";
            String username = userMapper.findUserByKeyword(namekeyword);
            totalnum = 1;
            allpage = 1;
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("id", 1);
            map.put("username", username);
            List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
            for (UnstructureData unstructureData : allDataSource) {
                String synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
                if (synchronizetable == null || synchronizetable.equals("")) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    continue;
                }
                int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
                if (istableexist == 0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    continue;
                }
                disproof += unstructureDataMapper.selectViewDisProof(synchronizetable, username);
                proof += unstructureDataMapper.selectViewReviewByUsername(synchronizetable, username);
                review += unstructureDataMapper.selectViewReviewByUsername(synchronizetable, username);
                callback += unstructureDataMapper.selectViewCallback(synchronizetable, username);
                rework += unstructureDataMapper.selectViewRework(synchronizetable, username);
                log.info("数据源" + unstructureData.getDataSourceName() + "查询成功！");
            }//遍历所有数据源的表
            map.put("disproof", disproof);
            map.put("proof", proof);
            map.put("review", review);
            map.put("callback", callback);
            map.put("rework", rework);
            map.put("newput", newput);
            list.add(map);
            jsonObject.put("list", list);
            jsonObject.put("total", totalnum);
            jsonObject.put("allpage", allpage);
            log.info("查询标注人员成功！");
        } else {
            List<User> userList = userMapper.selectAllUsernameAs(lsn, displayNumber);
            if (userList == null || userList.size() == 0) {
                log.info("未查询到标注人员信息！");
                return QueryResponse.genErr("未查询到标注人员信息！");
            }
            // List<User> user = userMapper.selectAllUsername();
            List<Map<String, Object>> list = new ArrayList<>();
            List<User> usernamelist = userMapper.selectAllUsername();
            totalnum = usernamelist.size();
            List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
            for (User u : userList) {
                Map<String, Object> map = new HashMap<>();
                String username = u.getUsername();
                map.put("id", u.getId());
                map.put("username", username);
                disproof = 0;
                proof = 0;
                review = 0;
                callback = 0;
                rework = 0;
                for (UnstructureData unstructureData : allDataSource) {
                    String synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
                    if (synchronizetable == null || synchronizetable.equals("")) {
                        log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                        response.setMsg("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                        continue;
                    }
                    int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
                    if (istableexist == 0) {
                        log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                        response.setMsg("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                        continue;
                    }
                    disproof += unstructureDataMapper.selectViewDisProof(synchronizetable, username);
                    proof += unstructureDataMapper.selectViewProofByUsername(synchronizetable, username);
                    review += unstructureDataMapper.selectViewReviewByUsername(synchronizetable, username);
                    callback += unstructureDataMapper.selectViewCallback(synchronizetable, username);
                    rework += unstructureDataMapper.selectViewRework(synchronizetable, username);
                }//遍历所有数据源的表
                map.put("disproof", disproof);
                map.put("proof", proof);
                map.put("review", review);
                map.put("callback", callback);
                map.put("rework", rework);
                map.put("newput", newput);
                list.add(map);
            }
            jsonObject.put("list", list);
            int add = totalnum % displayNumber;
            if (add == 0) {
                allpage = totalnum / displayNumber;
            } else {
                allpage = totalnum / displayNumber + 1;
            }
            jsonObject.put("total", totalnum);
            jsonObject.put("allpage", allpage);
            log.info("查询标注人员列表成功！");
        }
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询标注人员列表成功！");
        return response;
    }

    /*
    标注人员个人信息获取
    @param username
    @return username,password,bank_card_number,bank_of_deposit,id_number,phone_number,student_card_picture
     */
    public QueryResponse getDetail(String usernameId) {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String username = userMapper.findUsernameById(usernameId);
        User oneuser = userMapper.getDetailMessage(username);
        if (oneuser == null) {
            response.setSuccess(false);
            log.info("标注人员" + username + "详细信息查询失败！请核对用户名是否正确！");
            response.setMsg("标注人员" + username + "详细信息查询失败！请核对用户名是否正确！");
            return response;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("name", oneuser.getUsername());
        map.put("password", oneuser.getPassword());
        map.put("yhk", oneuser.getBank_card_number());
        map.put("kfc", oneuser.getBank_of_deposit());
        map.put("id_number", oneuser.getId_number());
        map.put("phone", oneuser.getPhone_number());
        map.put("student_card", oneuser.getStudent_card_picture());
        jsonObject.put("personmessage", map);
        log.info("标注人员" + username + "详细信息查询成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("标注人员" + username + "详细信息查询成功！");
        return response;
    }

    /*
    根据查询时间范围获取标注人员的工作内容：已标注数据量、未标注数据量、已审核数据量
    @param usernameId    用户id
    @param acquisitionTimeStart   筛选时间段的开始时间
    @param acquisitionTimeEnd     筛选时间段的结束时间
    @return acquisitionTimeStart
    @return acquisitionTimeEnd
    @return disproofnumber  未标注数据数量
    @return proofnumber     已标注数据数量
    @return reviewnumber    已审核数据数量
    @return callbacknumber     已打回数据数量
    @return reworknumber    已返工数据数量
     */
    public QueryResponse viewSelectMessage(String usernameId, Date acquisitionTimeStart, Date acquisitionTimeEnd) {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String username = userMapper.findUsernameById(usernameId);
        int proofnumber = 0;      //每个数据源对应表中标注人员工作结果数量统计
        int disproofnumber = 0;
        int reviewnumber = 0;
        int callbacknumber = 0;
        int reworknumber = 0;
        List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
        for (UnstructureData unstructureData : allDataSource) {
            String synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
            if (synchronizetable == null || synchronizetable.equals("")) {
                log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                response.setMsg("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                break;
            }
            int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
            if (istableexist == 0) {
                log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                response.setMsg("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                break;
            }
            proofnumber = proofnumber + unstructureDataMapper.selectViewProofByPeriod(synchronizetable, username, acquisitionTimeStart, acquisitionTimeEnd);
            disproofnumber = disproofnumber + unstructureDataMapper.selectViewDisProof(synchronizetable, username);
            reviewnumber = reviewnumber + unstructureDataMapper.selectViewReviewByPeriod(synchronizetable, username, acquisitionTimeStart, acquisitionTimeEnd);
            callbacknumber = callbacknumber + unstructureDataMapper.selectViewCallback(synchronizetable, username);
            reworknumber = reworknumber + unstructureDataMapper.selectViewRework(synchronizetable, username);
            log.info("数据源" + unstructureData.getDataSourceName() + "内工作情况查询完毕");
            response.setMsg("数据源" + unstructureData.getDataSourceName() + "内工作情况查询完毕");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", acquisitionTimeStart);
        map.put("endTime", acquisitionTimeEnd);
        map.put("disproof", disproofnumber);
        map.put("proof", proofnumber);
        map.put("review", reviewnumber);
        map.put("callback", callbacknumber);
        map.put("rework", reworknumber);
        map.put("newput", 0);
        map.put("money", 0);
        jsonObject.put("allmessage", map);
        log.info("标注人员" + username + "从" + acquisitionTimeStart + "至" + acquisitionTimeEnd + "的工作情况查询成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("标注人员" + username + "从" + acquisitionTimeStart + "至" + acquisitionTimeEnd + "的工作情况查询成功！");
        return response;
    }

    /*
    搜索标注人员姓名查看其工作情况
    @param datasourcetype  数据源类型 默认为1
    @param id    数据源id
    @param keywords 关键字 用于模糊查询标题
    @param status 数据状态
    @param proofreaderId 标注人id
    @param reviewerId 审核人id
    @param acquisitionTimeStart  采集开始时间
    @param acquisitionTimeEnd    采集结束时间 用于筛选采集时间 update_time
    @param scheduleTimeStart     分配开始时间
    @param scheduleTimeEnd       分配结束时间 用于筛选分配时间 schedule_time
    @param reviewTimeStart       审核开始时间
    @param reviewTimeEnd         审核结束时间 用于筛选审核时间 rev_time
    @param proofreadingTimeStart 标注开始时间
    @param proofreadingTimeEnd   标注结束时间 用于筛选标注时间 proof_time
    @return id     数据的id
    @return title  标题
    @return content 内容
    @return status 数据状态
    @return proofreader 标注人
    @return reviewer 审核人
    @rerurn updatetime  采集时间
    @rerurn scheduletime  分配时间
    @rerurn reviewtime   审核时间
    @rerurn prooftime    标注时间
      */
    //数据列表展示：
    // 初级展示的筛选条件：标注人、已通过/已标注/未标注/已打回/已返工  即从标注人员列表页展示的工作情况中进入数据列表页
    // 筛选条件为：标注人、审核人、数据源、数据状态（已通过/已标注/未标注/已打回/已返工/新上传）、采集时间、分配时间、标注时间、审核时间
    public QueryResponse showHasProof(int datasourcetype, String id, String keywords, int status, String proofreaderId, String reviewerId,
                                      Date acquisitionTimeStart, Date acquisitionTimeEnd,//采集时间 update_time
                                      Date scheduleTimeStart, Date scheduleTimeEnd,//分配时间 schedule_time
                                      Date reviewTimeStart, Date reviewTimeEnd,//审核时间 rev_time
                                      Date proofreadingTimeStart, Date proofreadingTimeEnd, Integer pageNumber, Integer displayNumber) {  //标注时间 proof_time
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String proofreader = userMapper.findUsernameById(proofreaderId);
        String reviewer = userMapper.findManagerById(reviewerId);
        // 查数据源表中同步数据列字段数据
        if (!keywords.equals("")) {
            keywords = keywords.trim();
            keywords = "%" + keywords + "%";
        }
        int totalnum = 0;
        if (datasourcetype != 1) {
            log.info("暂无数据！");
            return QueryResponse.genErr("暂无数据!");
        }
        int lsn = (pageNumber - 1) * displayNumber;
        if (id != null && id.length() != 0) {   //查询某一数据源的数据
            int datasourceid = Integer.parseInt(id);
            UnstructureData unstructureData = unstructureDataMapper.selectUnstructDataDatasourceById(datasourceid);
            String synchronizetable = unstructureData.getSyncTable();
            if (synchronizetable == null || synchronizetable.equals("")) {
                log.info("该同步表不存在！");
                return QueryResponse.genErr("该同步表不存在！");
            }
            int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
            if (istableexist == 0) {
                log.info("该数据源对应的同步表不存在!");
                return QueryResponse.genErr("该数据源对应的同步表不存在！");
            }

            //根据关键字、校对人、是否校对、标注时间、分配时间、采集时间、审核时间、审核人筛选同步表中的数据
            List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, lsn, displayNumber);
            totalnum = unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
            if (synchronizetablelist == null || synchronizetablelist.size() == 0) {
                log.info("未查询到同步表数据！");
                return QueryResponse.genErr("未查询到同步表数据!");
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> m : synchronizetablelist) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.get("id"));//数据在同步表里的id
                map.put("datasourceId", id);//数据源id
                map.put("datasourcename", unstructureData.getDataSourceName());//数据源名称
                if (m.get("title") == null || m.get("title").equals("")) {     //标题
                    map.put("title", "");
                } else {
                    map.put("title", m.get("title"));
                }
                if (m.get("content") == null || m.get("content").equals("")) {     //内容
                    map.put("content", "");
                } else {
                    map.put("content", m.get("content"));
                }
                if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                    map.put("status", "");
                } else {
                    map.put("status", m.get("status"));
                }
                if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                    map.put("updateTime", "");
                } else {
                    map.put("updateTime", m.get("update_time"));
                }
                if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                    map.put("scheduleTime", "");
                } else {
                    map.put("scheduleTime", m.get("schedule_time"));
                }
                if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                    map.put("proofTime", "");
                } else {
                    map.put("proofTime", m.get("proof_time"));
                }
                if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                    map.put("reviewTime", "");
                } else {
                    map.put("reviewTime", m.get("rev_time"));
                }
                if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                    map.put("proofreader", "");
                } else {
                    map.put("proofreader", m.get("proofreader"));
                }
                if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                    map.put("reviewer", "");
                } else {
                    map.put("reviewer", m.get("reviewer"));
                }
                list.add(map);

            }
            jsonObject.put("list", list);
        } else {  //TODO 遍历每个数据源
            //如果为空，就查找第一个数据源的数据
            List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
            List<Map<String, Object>> list = new ArrayList<>();
            List<Map<String, Object>> allDatalist = new ArrayList<>();
            for (UnstructureData unstructureData : allDataSource) {
                if (unstructureData.getIsdelete() != 0) {   //检查数据源是否已被删除
                    log.info("数据源" + unstructureData.getDataSourceName() + "已被删除！");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "已被删除！");
                    continue;
                }
                String synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
                if (synchronizetable == null || synchronizetable.equals("")) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    continue;
                }
                int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
                if (istableexist == 0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    continue;
                }
                List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, lsn, displayNumber);
                totalnum = totalnum + unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
                if (synchronizetablelist == null || synchronizetablelist.size() == 0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "中未查询到符合条件的数据！");
                    continue;
                }
                log.info("数据源" + unstructureData.getDataSourceName() + "中查询到符合条件的数据共" + totalnum + "条！");
                for (Map<String, Object> m : synchronizetablelist) {
                    Map<String, Object> map = new HashMap<>();   //每一条数据放进一个map
                    map.put("id", m.get("id"));    //数据的id
                    map.put("datasourceId", unstructureData.getId());//数据源id
                    map.put("datasourcename", unstructureData.getDataSourceName());
                    if (m.get("title") == null || m.get("title").equals("")) {     //标题
                        map.put("title", "");
                    } else {
                        map.put("title", m.get("title"));
                    }
                    if (m.get("content") == null || m.get("content").equals("")) {     //内容
                        map.put("content", "");
                    } else {
                        map.put("content", m.get("content"));
                    }
                    if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                        map.put("status", "");
                    } else {
                        map.put("status", m.get("status"));
                    }
                    if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                        map.put("updateTime", "");
                    } else {
                        map.put("updateTime", m.get("update_time"));
                    }
                    if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                        map.put("scheduleTime", "");
                    } else {
                        map.put("scheduleTime", m.get("schedule_time"));
                    }
                    if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                        map.put("proofTime", "");
                    } else {
                        map.put("proofTime", m.get("proof_time"));
                    }
                    if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                        map.put("reviewTime", "");
                    } else {
                        map.put("reviewTime", m.get("rev_time"));
                    }
                    if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                        map.put("proofreader", "");
                    } else {
                        map.put("proofreader", m.get("proofreader"));
                    }
                    if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                        map.put("reviewer", "");
                    } else {
                        map.put("reviewer", m.get("reviewer"));
                    }
                    list.add(map);

                }
                break;
//                if (list.size() == displayNumber) {
//                    allDatalist.addAll(list);
//                    list.clear();
//                    break;
//                } else {
//                    displayNumber = displayNumber - list.size();
//                }
            }
            jsonObject.put("list", list);
        }
        int allpage;
        int add = totalnum % displayNumber;
        if (add == 0) {
            allpage = totalnum / displayNumber;
        } else {
            allpage = totalnum / displayNumber + 1;
        }
        jsonObject.put("total", totalnum);
        jsonObject.put("allpage", allpage);
        log.info("查询成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询成功！");
        return response;
    }

    /**
     * 获取管理员列表
     *
     * @return 管理员id和name的list
     */
    public QueryResponse selectReviewer() {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<User> userList = userMapper.selectAllManagerName();
        if (userList == null || userList.size() == 0) {
            log.info("未查询到管理员数据！");
            return QueryResponse.genErr("未查询到管理员数据!");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : userList) {
            Map<String, Object> map = new HashMap<>();
            String id = String.valueOf(u.getId());
            String name = u.getUsername();
            map.put("id", id);
            map.put("username", name);
            list.add(map);
        }
        jsonObject.put("managerIdNameList", list);
        log.info("查询所有管理员成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询所有管理员成功！");
        return response;
    }

    /**
     * 审核修改三元组状态
     *
     * @param id     三元组id
     * @param status 三元组状态
     * @return 修改是否成功的结果
     */
    public QueryResponse reviewSetStatus(String id, String status) {
        QueryResponse response = new QueryResponse();
        int isok = verifyMapper.changeStatus(id, status);//status： 空是未审核 0是打回 1是通过
        if (isok == 0) {
            response.setMsg("修改三元组状态失败");
            response.setSuccess(false);
            return response;
        }
        response.setMsg("修改三元组状态成功");
        response.setSuccess(true);
        return response;
    }

    public QueryResponse showHasProof1(int datasourcetype, String id, String keywords, int status, String proofreaderId, String reviewerId,
                                       Date acquisitionTimeStart, Date acquisitionTimeEnd,//采集时间 update_time
                                       Date scheduleTimeStart, Date scheduleTimeEnd,//分配时间 schedule_time
                                       Date reviewTimeStart, Date reviewTimeEnd,//审核时间 rev_time
                                       Date proofreadingTimeStart, Date proofreadingTimeEnd, Integer pageNumber, Integer displayNumber) {  //标注时间 proof_time
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String proofreader = userMapper.findUsernameById(proofreaderId);
        String reviewer = userMapper.findManagerById(reviewerId);
        // 查数据源表中同步数据列字段数据
        if (!keywords.equals("")) {
            keywords = keywords.trim();
            keywords = "%" + keywords + "%";
        }
        int totalnum = 0;
        if (datasourcetype != 1) {
            log.info("暂无数据！");
            return QueryResponse.genErr("暂无数据!");
        }
        int lsn = (pageNumber - 1) * displayNumber;
        if (id != null && id.length() != 0) {   //查询某一数据源的数据
            int datasourceid = Integer.parseInt(id);
            UnstructureData unstructureData = unstructureDataMapper.selectUnstructDataDatasourceById(datasourceid);
            String synchronizetable = unstructureData.getSyncTable();
            if (synchronizetable == null || synchronizetable.equals("")) {
                log.info("该同步表不存在！");
                return QueryResponse.genErr("该同步表不存在！");
            }
            int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
            if (istableexist == 0) {
                log.info("该数据源对应的同步表不存在!");
                return QueryResponse.genErr("该数据源对应的同步表不存在！");
            }

            //根据关键字、校对人、是否校对、标注时间、分配时间、采集时间、审核时间、审核人筛选同步表中的数据
            List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, lsn, displayNumber);
            totalnum = unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
            if (synchronizetablelist == null || synchronizetablelist.size() == 0) {
                log.info("未查询到同步表数据！");
                return QueryResponse.genErr("未查询到同步表数据!");
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> m : synchronizetablelist) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.get("id"));//数据在同步表里的id
                map.put("datasourceId", id);//数据源id
                map.put("datasourcename", unstructureData.getDataSourceName());//数据源名称
                if (m.get("title") == null || m.get("title").equals("")) {     //标题
                    map.put("title", "");
                } else {
                    map.put("title", m.get("title"));
                }
                if (m.get("content") == null || m.get("content").equals("")) {     //内容
                    map.put("content", "");
                } else {
                    map.put("content", m.get("content"));
                }
                if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                    map.put("status", "");
                } else {
                    map.put("status", m.get("status"));
                }
                if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                    map.put("updateTime", "");
                } else {
                    map.put("updateTime", m.get("update_time"));
                }
                if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                    map.put("scheduleTime", "");
                } else {
                    map.put("scheduleTime", m.get("schedule_time"));
                }
                if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                    map.put("proofTime", "");
                } else {
                    map.put("proofTime", m.get("proof_time"));
                }
                if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                    map.put("reviewTime", "");
                } else {
                    map.put("reviewTime", m.get("rev_time"));
                }
                if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                    map.put("proofreader", "");
                } else {
                    map.put("proofreader", m.get("proofreader"));
                }
                if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                    map.put("reviewer", "");
                } else {
                    map.put("reviewer", m.get("reviewer"));
                }
                list.add(map);

            }
            jsonObject.put("list", list);
        } else {  //遍历每个数据源
            List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
            List<Map<String, Object>> list = new ArrayList<>();
            List<Map<String, Object>> allDatalist = new ArrayList<>();
            for (UnstructureData unstructureData : allDataSource) {
                if (unstructureData.getIsdelete() != 0) {   //检查数据源是否已被删除
                    log.info("数据源" + unstructureData.getDataSourceName() + "已被删除！");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "已被删除！");
                    continue;
                }
                String synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
                if (synchronizetable == null || synchronizetable.equals("")) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
                    continue;
                }
                int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
                if (istableexist == 0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    response.setMsg("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
                    continue;
                }
                //当前数据源符合条件的数据总数
                int rightNowTotalNum = unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords,
                        acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
                //之前数据源数据总数
                int beforTotalNum = totalnum;
                //数据总数累加
                totalnum = totalnum + rightNowTotalNum;
                //分三种情况讨论，一是总数大于需要展示的数量   如：第一页   数据源1有15条    第三页  数据源1+2有35条
                if (totalnum >= pageNumber * displayNumber) {
                    List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader,
                            reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart,
                            proofreadingTimeEnd, lsn, displayNumber);

                } else if (totalnum <= (pageNumber - 1) * displayNumber) {
                    break;
                } else {
                    //当前数据源的数据条数
                    List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader,
                            reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart,
                            proofreadingTimeEnd, lsn, displayNumber);
                }
                List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader,
                        reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart,
                        proofreadingTimeEnd, lsn, displayNumber);

                if (synchronizetablelist == null || synchronizetablelist.size() == 0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "中未查询到符合条件的数据！");
                    continue;
                }
                log.info("数据源" + unstructureData.getDataSourceName() + "中查询到符合条件的数据共" + totalnum + "条！");
                for (Map<String, Object> m : synchronizetablelist) {
                    Map<String, Object> map = new HashMap<>();   //每一条数据放进一个map
                    map.put("id", m.get("id"));    //数据的id
                    map.put("datasourceId", unstructureData.getId());//数据源id
                    map.put("datasourcename", unstructureData.getDataSourceName());
                    if (m.get("title") == null || m.get("title").equals("")) {     //标题
                        map.put("title", "");
                    } else {
                        map.put("title", m.get("title"));
                    }
                    if (m.get("content") == null || m.get("content").equals("")) {     //内容
                        map.put("content", "");
                    } else {
                        map.put("content", m.get("content"));
                    }
                    if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                        map.put("status", "");
                    } else {
                        map.put("status", m.get("status"));
                    }
                    if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                        map.put("updateTime", "");
                    } else {
                        map.put("updateTime", m.get("update_time"));
                    }
                    if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                        map.put("scheduleTime", "");
                    } else {
                        map.put("scheduleTime", m.get("schedule_time"));
                    }
                    if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                        map.put("proofTime", "");
                    } else {
                        map.put("proofTime", m.get("proof_time"));
                    }
                    if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                        map.put("reviewTime", "");
                    } else {
                        map.put("reviewTime", m.get("rev_time"));
                    }
                    if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                        map.put("proofreader", "");
                    } else {
                        map.put("proofreader", m.get("proofreader"));
                    }
                    if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                        map.put("reviewer", "");
                    } else {
                        map.put("reviewer", m.get("reviewer"));
                    }
                    list.add(map);

                }
                if (list.size() == displayNumber) {
                    allDatalist.addAll(list);
                    list.clear();
                    break;
                } else {
                    displayNumber = displayNumber - list.size();

                }
            }
            jsonObject.put("list", allDatalist);
        }
        int allpage;
        int add = totalnum % displayNumber;
        if (add == 0) {
            allpage = totalnum / displayNumber;
        } else {
            allpage = totalnum / displayNumber + 1;
        }
        jsonObject.put("total", totalnum);
        jsonObject.put("allpage", allpage);
        log.info("查询成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询成功！");
        return response;
    }

    public QueryResponse showHasProof2(int datasourcetype, String id, String keywords, int status, String proofreaderId, String reviewerId,
                                       Date acquisitionTimeStart, Date acquisitionTimeEnd,//采集时间 update_time
                                       Date scheduleTimeStart, Date scheduleTimeEnd,//分配时间 schedule_time
                                       Date reviewTimeStart, Date reviewTimeEnd,//审核时间 rev_time
                                       Date proofreadingTimeStart, Date proofreadingTimeEnd, Integer pageNumber, Integer displayNumber) {  //标注时间 proof_time
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String proofreader = userMapper.findUsernameById(proofreaderId);
        String reviewer = userMapper.findManagerById(reviewerId);
        // 查数据源表中同步数据列字段数据
        if (!keywords.equals("")) {
            keywords = keywords.trim();
            keywords = "%" + keywords + "%";
        }
        int totalnum = 0;
        if (datasourcetype != 1) {
            log.info("暂无数据！");
            return QueryResponse.genErr("暂无数据!");
        }

        int lsn = (pageNumber - 1) * displayNumber;
        if (id != null && id.length() != 0) {   //查询某一数据源的数据
            int datasourceid = Integer.parseInt(id);
            UnstructureData unstructureData = unstructureDataMapper.selectUnstructDataDatasourceById(datasourceid);
            String synchronizetable = unstructureData.getSyncTable();
            if (synchronizetable == null || synchronizetable.equals("")) {
                log.info("该同步表不存在！");
                return QueryResponse.genErr("该同步表不存在！");
            }
            int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);
            if (istableexist == 0) {
                log.info("该数据源对应的同步表不存在!");
                return QueryResponse.genErr("该数据源对应的同步表不存在！");
            }

            //根据关键字、校对人、是否校对、标注时间、分配时间、采集时间、审核时间、审核人筛选同步表中的数据
            List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, lsn, displayNumber);
            totalnum = unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords, acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
            if (synchronizetablelist == null || synchronizetablelist.size() == 0) {
                log.info("未查询到同步表数据！");
                return QueryResponse.genErr("未查询到同步表数据!");
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> m : synchronizetablelist) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.get("id"));//数据在同步表里的id
                map.put("datasourceId", id);//数据源id
                map.put("datasourcename", unstructureData.getDataSourceName());//数据源名称
                if (m.get("title") == null || m.get("title").equals("")) {     //标题
                    map.put("title", "");
                } else {
                    map.put("title", m.get("title"));
                }
                if (m.get("content") == null || m.get("content").equals("")) {     //内容
                    map.put("content", "");
                } else {
                    map.put("content", m.get("content"));
                }
                if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                    map.put("status", "");
                } else {
                    map.put("status", m.get("status"));
                }
                if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                    map.put("updateTime", "");
                } else {
                    map.put("updateTime", m.get("update_time"));
                }
                if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                    map.put("scheduleTime", "");
                } else {
                    map.put("scheduleTime", m.get("schedule_time"));
                }
                if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                    map.put("proofTime", "");
                } else {
                    map.put("proofTime", m.get("proof_time"));
                }
                if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                    map.put("reviewTime", "");
                } else {
                    map.put("reviewTime", m.get("rev_time"));
                }
                if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                    map.put("proofreader", "");
                } else {
                    map.put("proofreader", m.get("proofreader"));
                }
                if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                    map.put("reviewer", "");
                } else {
                    map.put("reviewer", m.get("reviewer"));
                }
                list.add(map);

            }
            jsonObject.put("list", list);
        } else {  //遍历每个数据源
            List<UnstructureData> allDataSource = unstructureDataMapper.selectUnstructureDatasourceName();
            List<Map<String, Object>> list = new ArrayList<>();
            List<Map<String, Object>> allDatalist = new ArrayList<>();
            int beforeNum = 0;
            Map<UnstructureData, Integer> newlsn =new HashMap<>();
            newlsn.put(allDataSource.get(0),lsn);
            int i=0;
            //计算之前数据源的数据一共占据的页数
            for (UnstructureData unstructureData = allDataSource.get(i);i < allDataSource.size();i++) {
                String synchronizetable = getDataSourseSyName(unstructureData);
                if(synchronizetable==null || synchronizetable==""){
                    log.info("数据源" + unstructureData.getDataSourceName() + "数据表不存在！");
                    continue;
                }
                beforeNum = totalnum;
                //之前数据源占据的页数
                int occupyPageNum = getPageNumber(beforeNum,displayNumber);
                //计算开始数据条数
                if (lsn>beforeNum){//
                    int newlsnn = (pageNumber-1-occupyPageNum)*displayNumber+(lsn - totalnum +1);
                }else {
                    int newlsnn = totalnum - lsn;//
                }
                //当前数据源符合条件的数据数量
                int num1 = unstructureDataMapper.selectAllUnstrutrueDataNumberByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords,
                        acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart, reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd);
                if (num1 ==0) {
                    log.info("数据源" + unstructureData.getDataSourceName() + "中未查询到符合条件的数据！");
                    continue;
                }
                totalnum = totalnum + num1;
                //之前数据源加当前数据源占据的页数
                int nowPageNum = getPageNumber(totalnum,displayNumber);
                if(nowPageNum<pageNumber){
                    continue;
                }else if(occupyPageNum+1==pageNumber){

                }
                List<Map<String, Object>> synchronizetablelist = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(synchronizetable, status, proofreader, reviewer, keywords,
                        acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart,
                        reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, newlsn.get(unstructureData), displayNumber);
                //如果需要补充数据
                if (synchronizetablelist.size() < displayNumber && pageNumber == occupyPageNum) {
                    String unstructureDataNextTableName="";
                    //遍历后续数据源进行补充直到达到数量
                    for(UnstructureData unstructureDataNext = allDataSource.get(i+1);i< allDataSource.size() && synchronizetablelist.size()<displayNumber;i++){
                        unstructureDataNextTableName= getDataSourseSyName(unstructureDataNext);
                        if(unstructureDataNextTableName !=""){
                            break;
                        }
                        int needNum = displayNumber - synchronizetablelist.size();
                        List<Map<String, Object>> synchronizetablelist2 = unstructureDataMapper.selectAllUnstrutrueDataByFourTimeAndUsername(unstructureDataNextTableName,status,proofreader,reviewer, keywords,
                                acquisitionTimeStart, acquisitionTimeEnd, scheduleTimeStart, scheduleTimeEnd, reviewTimeStart,
                                reviewTimeEnd, proofreadingTimeStart, proofreadingTimeEnd, 0, needNum);
                        synchronizetablelist.addAll(synchronizetablelist2);
                        newlsn.put(unstructureDataNext,getStartNum(displayNumber,needNum,pageNumber,getPageNumber(totalnum,displayNumber)));
                    }

                }
                log.info("数据源" + unstructureData.getDataSourceName() + "中查询到符合条件的数据共" + totalnum + "条！");
                for (Map<String, Object> m : synchronizetablelist) {
                    Map<String, Object> map = new HashMap<>();   //每一条数据放进一个map
                    map.put("id", m.get("id"));    //数据的id
                    map.put("datasourceId", unstructureData.getId());//数据源id
                    map.put("datasourcename", unstructureData.getDataSourceName());
                    if (m.get("title") == null || m.get("title").equals("")) {     //标题
                        map.put("title", "");
                    } else {
                        map.put("title", m.get("title"));
                    }
                    if (m.get("content") == null || m.get("content").equals("")) {     //内容
                        map.put("content", "");
                    } else {
                        map.put("content", m.get("content"));
                    }
                    if (m.get("status") == null || m.get("status").equals("")) { //数据状态
                        map.put("status", "");
                    } else {
                        map.put("status", m.get("status"));
                    }
                    if (m.get("update_time") == null || m.get("update_time").equals("")) { //采集时间，数据同步至系统的时间
                        map.put("updateTime", "");
                    } else {
                        map.put("updateTime", m.get("update_time"));
                    }
                    if (m.get("schedule_time") == null || m.get("schedule_time").equals("")) {//分配时间
                        map.put("scheduleTime", "");
                    } else {
                        map.put("scheduleTime", m.get("schedule_time"));
                    }
                    if (m.get("proof_time") == null || m.get("proof_time").equals("")) {//标注时间
                        map.put("proofTime", "");
                    } else {
                        map.put("proofTime", m.get("proof_time"));
                    }
                    if (m.get("rev_time") == null || m.get("rev_time").equals("")) {//审核时间
                        map.put("reviewTime", "");
                    } else {
                        map.put("reviewTime", m.get("rev_time"));
                    }
                    if (m.get("proofreader") == null || m.get("proofreader").equals("")) {//标注人
                        map.put("proofreader", "");
                    } else {
                        map.put("proofreader", m.get("proofreader"));
                    }
                    if (m.get("reviewer") == null || m.get("reviewer").equals("")) {//审核人
                        map.put("reviewer", "");
                    } else {
                        map.put("reviewer", m.get("reviewer"));
                    }
                    list.add(map);

                }

            }
            jsonObject.put("list", allDatalist);
        }
        int allpage = getPageNumber(totalnum,displayNumber);
        jsonObject.put("total", totalnum);
        jsonObject.put("allpage", allpage);
        log.info("查询成功！");
        response.setSuccess(true);
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        response.setMsg("查询成功！");
        return response;
    }

    public String getDataSourseSyName(UnstructureData unstructureData) {
        String synchronizetable = "";
        if (unstructureData.getIsdelete() != 0) {   //检查数据源是否已被删除
            log.info("数据源" + unstructureData.getDataSourceName() + "已被删除！");
        }
        synchronizetable = unstructureData.getSyncTable();//检查表名是否存在
        if (synchronizetable == null || synchronizetable.equals("")) {
            log.info("数据源" + unstructureData.getDataSourceName() + "同步表不存在！");
        }
        int istableexist = unstructureDataMapper.isSynchronizeTableExist(synchronizetable);//检查表名对应的表是否创建
        if (istableexist == 0) {
            log.info("数据源" + unstructureData.getDataSourceName() + "对应的同步表不存在!");
        }
        return synchronizetable;

    }

    public int getPageNumber(int totalnum,int displayNumber){
        int allpage = 0 ;
        int add = totalnum % displayNumber;
        if (add == 0) {
            allpage = totalnum / displayNumber;
        } else {
            allpage = totalnum / displayNumber + 1;
        }
        return allpage;
    }

    public int getStartNum(int displayNumber,int needNum,int pageNum,int flagNum){
        int start = 0;
        if(pageNum>flagNum) {
            start = (pageNum - flagNum -1) * displayNumber + needNum + 1;
        }
        return start;
    }
}

