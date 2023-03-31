package com.edu.bistu.datacollectproofaudit.controller;



import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.service.DistributionService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author fuanna
 *数据分配
 */
@RestController
public class DataDistributionController {

    private final DistributionService distributionService;


    public DataDistributionController(@Autowired DistributionService distributionService){
        this.distributionService = distributionService;
    }

    /**
     * 显示所有非结构化数据源名称
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/selectunstructuredatasource",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectUnstructureDatasource() {
        return distributionService.selectUnstructureDatasource();
    }


    /**
     * 选择校对人
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/selectproofreader",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectProofreader() {
        return distributionService.selectproofreader();
    }


    /**
     * 选择分配的数据
     * @param id 数据源ID
     * @param pageNumber 第几页
     * @param displayNumber 展示几页
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/selectdata",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectdata(@RequestParam(name = "id",defaultValue = "") Integer id,
                                    @RequestParam(name = "pageNumber",defaultValue = "1") int pageNumber,
                                    @RequestParam(name = "displayNumber",defaultValue = "10") int displayNumber) {
        return distributionService.selectdata(id,pageNumber,displayNumber);
    }


    /**
     * 根据起止序号筛选数据(未用)
     * @param id 数据源ID
     * @param start 开始序号
     * @param end 结束序号
     * @param pageNumber 第几页
     * @param displayNumber 展示几页
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/selectdatabyserialnumber",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectdataBySerialNumber(@RequestParam(name = "id",defaultValue = "") Integer id,
                                            @RequestParam(name = "start",defaultValue = "") int start,
                                            @RequestParam(name = "end",defaultValue = "") int end,
                                            @RequestParam(name = "pageNumber",defaultValue = "1") int pageNumber,
                                            @RequestParam(name = "displayNumber",defaultValue = "10") int displayNumber) {
        return distributionService.selectdataBySerialNumber(id,start,end,pageNumber,displayNumber);
    }


    /**
     * 根据起止序号更新同步表分配
     * @param id 数据源ID
     * @param proofreader 校对人
     * @param start 开始序号
     * @param end 结束序号
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/updateSerialNmberDistribution",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse updateSerialNmberDistribution(@RequestParam(name = "id",defaultValue = "") Integer id,
                                                       @RequestParam(name = "proofreader",defaultValue = "") String proofreader,
                                                       @RequestParam(name = "start",defaultValue = "") int start,
                                                       @RequestParam(name = "end",defaultValue = "") int end) {
        return distributionService.updateSerialNmberDistribution(id,proofreader,start,end);
    }

    /**
     * 更新同步表中的分配
     * @param id 数据源ID
     * @param proofreader 校对人
     * @param ids id列表
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/datadistribution/updatedistribution",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse updateDistribution(@RequestParam(name = "id",defaultValue = "") Integer id,
                                            @RequestParam(name = "proofreader",defaultValue = "") String proofreader,
                                            @RequestParam(name = "ids",defaultValue = "") List<Integer> ids) {
        return distributionService.updateDistribution(id,proofreader,ids);
    }


}
