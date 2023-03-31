package com.edu.bistu.datacollectproofaudit.controller;

import com.edu.bistu.datacollectproofaudit.service.UserService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.edu.bistu.datacollectproofaudit.utils.RedisUtils;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/*

登陆方法
@author wtt
@param username 用户名
@param password 密码
@param usertype 用户类型 1是管理员 0是标注人员
 */
@RestController
public class UserLoginController {
    @Autowired
    private UserService userService;

    private RedisUtils redisUtils;

    @RequestMapping(value = "/login",produces = "application/json;charset=UTF-8",method = RequestMethod.POST)
    public QueryResponse login(@RequestParam(value = "username", defaultValue = "") String username,
                               @RequestParam(value = "password", defaultValue = "") String password,
                               @RequestParam(value = "usertype", defaultValue = "") Boolean usertype ){

        return userService.login(username,password,usertype);
    }
}
