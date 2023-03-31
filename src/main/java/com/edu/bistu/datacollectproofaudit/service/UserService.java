package com.edu.bistu.datacollectproofaudit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.pojo.User;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.edu.bistu.datacollectproofaudit.utils.RedisUtils;
import com.edu.bistu.datacollectproofaudit.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.edu.bistu.datacollectproofaudit.utils.TokenUtils.*;

/*
@author wtt
标注人员和管理人员登陆功能
@param username  用户名
@param password  密码
@param usertype  用户类型 标注人员是0，管理员是1
@return token
 */
@Service
public class UserService {
    private final UserMapper userMapper;

    private RedisUtils redisUtils;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(@Autowired UserMapper userMapper,
                       @Autowired RedisUtils redisUtils){
        this.userMapper=userMapper;
        this.redisUtils=redisUtils;
    }


    public QueryResponse login(String username, String password, Boolean usertype){
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        if(usertype){                                     //默认管理员user_type为1，标注人员为0
            if(userMapper.loginManager(username, password)){
                User as = userMapper.findManagerByName(username);
//                User booster = new User();
//                booster.setId(as.getId());
//                booster.setUsername(as.getUsername());
//                booster.setPassword(as.getPassword());
                HashMap<String,String> claim = new HashMap<>();
                claim.put("name",as.getUsername());
                //String token = getToken(booster);    //生成一个签名
                String token = TokenUtils.createToken(claim,as);
                redisUtils.set(token,token,4*60*60); //redis2*60秒过期
                jsonObject.put("token",token);
                jsonObject.put("id",as.getId());
                jsonObject.put("name",as.getUsername());
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
                log.info("管理员"+username+"登陆成功！");
                response.setMsg("管理员"+username+"登陆成功！");
            } else {
                log.info("登陆失败！账号或密码错误！");
                response.setMsg("登陆失败！账号或密码错误！");
                response.setSuccess(false);
                return response;
            }
            response.setSuccess(true);
        }
        else {
            if (userMapper.login(username, password)){
                User as = userMapper.findUserByName(username);
//                User booster = new User();
//                booster.setId(as.getId());
//                booster.setUsername(as.getUsername());
//                booster.setPassword(as.getPassword());
                //String token = getToken(booster);    //生成一个签名
                HashMap<String,String> claim = new HashMap<>();
                claim.put("name",as.getUsername());
                String token = TokenUtils.createToken(claim,as);
                redisUtils.set(token,token,2*60); //redis20*60秒过期
                jsonObject.put("token",token);
                jsonObject.put("id",as.getId());
                jsonObject.put("name",as.getUsername());
                response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
                log.info("用户"+username+"登陆成功！");
                response.setMsg("用户"+username+"登陆成功！");
            } else {
                log.info("登陆失败！账号或密码错误！");
                response.setMsg("登陆失败！账号或密码错误！");
                response.setSuccess(false);
                return response;
            }
            response.setSuccess(true);

        }
        return response;

    }
}
