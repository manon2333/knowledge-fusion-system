package com.edu.bistu.datacollectproofaudit.Interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.edu.bistu.datacollectproofaudit.annotation.PassToken;
import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.mapper.UserMapper;
import com.edu.bistu.datacollectproofaudit.pojo.User;
import com.edu.bistu.datacollectproofaudit.service.UserService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import com.edu.bistu.datacollectproofaudit.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import static com.edu.bistu.datacollectproofaudit.utils.TokenUtils.*;
import com.edu.bistu.datacollectproofaudit.utils.RedisUtils;
import com.edu.bistu.datacollectproofaudit.utils.StringUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 @description：拦截器
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

        @Autowired
        UserService userService;
        @Autowired
        UserMapper userMapper;
        @Autowired
        RedisUtils redisUtils;

        private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

        @Override
        public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
            String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
            // 如果不是映射到方法直接通过
            if (!(object instanceof HandlerMethod)) {
                return true;
            }
            HandlerMethod handlerMethod = (HandlerMethod) object;
            Method method = handlerMethod.getMethod();
            //检查是否有passtoken注释，有则跳过认证
            if (method.isAnnotationPresent(PassToken.class)) {
                PassToken passToken = method.getAnnotation(PassToken.class);
                if (passToken.required()) {
                    return true;
                }
            }
            //检查有没有需要用户权限的注解
            if (method.isAnnotationPresent(UserLoginToken.class)) {
                UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
                if (userLoginToken.required()) {
                    // 执行认证
                    if (token == null || token == "") {
                        log.info("无token，请重新登录!");
                        httpServletResponse.setStatus(401);
                        return false;
                    }
                    // 获取 token 中的 user id
                    Integer userId = getTokenUserId();
                    User user = userMapper.findUserById(userId);
                    User user1 = userMapper.findManagerNameById(userId);
                    if (user == null&&user1 ==null) {
                        log.info("用户不存在，请重新登录");
                        httpServletResponse.setStatus(401);
                        return false;
                    }
                    // 验证 token
//                    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
//                    try {
//                        jwtVerifier.verify(token);
//                    } catch (JWTVerificationException e) {
//                        log.info("验证失败！");
//                        httpServletResponse.setStatus(401);
//                        return false;
//                    }
                    try {
                        String redisToken = (String)redisUtils.get(token);
                        if(StringUtils.hasText(redisToken)){
                            Integer time = Math.toIntExact(redisUtils.getExpire(token));
                            if (time>0&&time<10*60){    //时间小于10min就续期30分钟
                                System.out.println("续期redis");
                                redisUtils.expire(redisToken,30*60);
                                log.info("续期成功");
                            }
                            return true;
                        }
                        try{
                            TokenUtils.verifyToken(token);
                        }catch (Exception e){
                            e.printStackTrace();
                            log.info("验证失败！");
                            httpServletResponse.setStatus(401);
                            return false;
                        }
                        return true;
                    }catch (Exception e){
                        e.printStackTrace();
                        httpServletResponse.setStatus(401);
                    }

                }
            }
            return true;


    }
}

