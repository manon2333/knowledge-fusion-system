package com.edu.bistu.datacollectproofaudit.controller;

import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;


@RestController
public class KnowledgeFuseController {


    /**
     * 选择源知识图谱和目标知识图谱，点击实体对齐，出现待对齐实体的候选实体
     * @param sourceKG 源知识图谱名
     * @param targetKG 目标知识图谱名
     * @return
     */
    @UserLoginToken
    @RequestMapping(value = "/proofreading/entityAlighment",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse entityAlighment(@RequestParam(name = "sourceKG",defaultValue = "1") String sourceKG,//源知识图谱名称
                                             @RequestParam(name = "targetKG",defaultValue = "") String targetKG){//关键字


        //返回源知识图谱中每一个实体对应的目标知识图谱候选实体
        return null;

    }





}
