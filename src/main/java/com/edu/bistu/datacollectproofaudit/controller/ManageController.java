package com.edu.bistu.datacollectproofaudit.controller;

import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.service.ManageService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
public class ManageController {
    @Autowired
    private ManageService manageService;

    //标注人员工作情况列表页
    @UserLoginToken
    @RequestMapping(value = "/message",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse showMessage(@RequestParam(name = "namekeyword",defaultValue = "") String namekeyword,
                                     @RequestParam(name = "pageNumber",defaultValue = "1") Integer pageNumber,//页数
                                     @RequestParam(name = "displayNumber",defaultValue = "10") Integer displayNumber){
        return manageService.getMessage(namekeyword,pageNumber,displayNumber);
    }
    //标注人员个人信息展示
    @UserLoginToken
    @RequestMapping(value = "/message/detail",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse showDetailMessage(@RequestParam(value = "usernameId", defaultValue = "") String usernameId){
        return manageService.getDetail(usernameId);
    }
    //筛选时间段内某一标注人员的工作情况
    @UserLoginToken
    @RequestMapping(value = "/message/viewdata", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    public QueryResponse viewData(@RequestParam(value = "usernameId", defaultValue = "") String usernameId,
                                  @RequestParam(name = "acquisitionTimeStart", defaultValue = "") String acquisitionTimeStart,
                                  @RequestParam(name = "acquisitionTimeEnd", defaultValue = "") String acquisitionTimeEnd)
    {
        Timestamp acquisitionTimeStartDate=null;
        Timestamp acquisitionTimeEndDate=null;
        if(!acquisitionTimeStart.equals("")){
            acquisitionTimeStartDate=Timestamp.valueOf(acquisitionTimeStart);
        }
        if(!acquisitionTimeEnd.equals("")){
            acquisitionTimeEndDate=Timestamp.valueOf(acquisitionTimeEnd);
        }
        return manageService.viewSelectMessage(usernameId,acquisitionTimeStartDate,acquisitionTimeEndDate);
    }

//    //近三天、近一周、近一月的工作情况
//    @RequestMapping(value = "/message/relativework", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
//    public QueryResponse relativeWork(@RequestParam(name = "usernameId",defaultValue = "") String usernameId,
//                                      @RequestParam(name = "perior",defaultValue = "-3") int perior){
//        return manageService.selectRelativeThreeDay(usernameId,perior);
//    }

//    //查询某一标注人员工作情况
//    @RequestMapping(value = "/message/searchuser", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
//    public QueryResponse searchUser(@RequestParam(name = "username",defaultValue = "") String username){
//        return manageService.searchUser(username);
//    }

    //数据列表展示
    @UserLoginToken
    @RequestMapping(value = "/message/selectdatalist",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse viewDatasource(@RequestParam(name = "datasourcetype",defaultValue = "1") int datasourcetype,//数据源类型
                                        @RequestParam(name = "id",defaultValue = "") String id,//ID
                                        @RequestParam(name = "keywords",defaultValue = "") String keywords,//关键字
                                        @RequestParam(name = "proofreaderId",defaultValue = "") String proofreaderId,//操作人ID
                                        @RequestParam(name = "status",defaultValue = "0") int status,//校对状态
                                        @RequestParam(name = "acquisitionTimeStart",defaultValue = "") String acquisitionTimeStart,//采集开始时间
                                        @RequestParam(name = "acquisitionTimeEnd",defaultValue = "") String acquisitionTimeEnd,//采集结束时间
                                        @RequestParam(name = "scheduleTimeStart",defaultValue = "") String scheduleTimeStart,//分配开始时间
                                        @RequestParam(name = "scheduleTimeEnd",defaultValue = "") String scheduleTimeEnd,//分配结束时间
                                        @RequestParam(name = "reviewTimeStart",defaultValue = "") String reviewTimeStart,//审核开始时间
                                        @RequestParam(name = "reviewTimeEnd",defaultValue = "") String reviewTimeEnd,//审核结束时间
                                        @RequestParam(name = "proofreadingTimeStart",defaultValue = "") String proofreadingTimeStart,//校对开始时间
                                        @RequestParam(name = "proofreadingTimeEnd",defaultValue = "") String proofreadingTimeEnd,//校对结束时间
                                        @RequestParam(name = "reviewerId",defaultValue = "") String reviewerId,
                                        @RequestParam(name = "pageNumber",defaultValue = "1") Integer pageNumber,//页数
                                        @RequestParam(name = "displayNumber",defaultValue = "10") Integer displayNumber) {//审核人
        Timestamp acquisitionTimeStartDate=null;
        Timestamp acquisitionTimeEndDate=null;
        Timestamp scheduleTimeStartDate=null;
        Timestamp scheduleTimeEndDate=null;
        Timestamp reviewTimeStartDate=null;
        Timestamp reviewTimeEndDate=null;
        Timestamp proofreadingTimeStartDate=null;
        Timestamp proofreadingTimeEndDate=null;
        if(!acquisitionTimeStart.equals("")){
            acquisitionTimeStartDate=Timestamp.valueOf(acquisitionTimeStart);
        }
        if(!acquisitionTimeEnd.equals("")){
            acquisitionTimeEndDate=Timestamp.valueOf(acquisitionTimeEnd);
        }
        if(!scheduleTimeStart.equals("")){
            scheduleTimeStartDate=Timestamp.valueOf(scheduleTimeStart);
        }
        if(!scheduleTimeEnd.equals("")){
            scheduleTimeEndDate=Timestamp.valueOf(scheduleTimeEnd);
        }
        if(!reviewTimeStart.equals("")){
            reviewTimeStartDate=Timestamp.valueOf(reviewTimeStart);
        }
        if(!reviewTimeEnd.equals("")){
            reviewTimeEndDate=Timestamp.valueOf(reviewTimeEnd);
        }
        if(!proofreadingTimeStart.equals("")){
            proofreadingTimeStartDate=Timestamp.valueOf(proofreadingTimeStart);
        }
        if(!proofreadingTimeEnd.equals("")){
            proofreadingTimeEndDate=Timestamp.valueOf(proofreadingTimeEnd);
        }
      return manageService.showHasProof(datasourcetype,id,keywords,status,proofreaderId,reviewerId,acquisitionTimeStartDate,acquisitionTimeEndDate,scheduleTimeStartDate,scheduleTimeEndDate,reviewTimeStartDate,reviewTimeEndDate,proofreadingTimeStartDate,proofreadingTimeEndDate,pageNumber,displayNumber);
     }

    @UserLoginToken
    @RequestMapping(value = "/message/selectReviewer",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse selectProofreader(){
        return manageService.selectReviewer();
    }

    /**
     * 审核时对三元组进行的状态更改
     * @param id 三元组ID
     * @param status 数据状态 null未审核 0不通过  1通过
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value = "/message/changeStatus",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse changeStatus(@RequestParam(value = "id", defaultValue = "") String id,
                                      @RequestParam(value = "status", defaultValue = "")String status){
        return manageService.reviewSetStatus(id,status);
    }
    @UserLoginToken
    @RequestMapping(value = "/firstPage",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse firstPage(){
        QueryResponse response = new QueryResponse();
        response.setMsg("欢迎您！");
        response.setSuccess(true);
        return response;
    }


}
