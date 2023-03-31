package com.edu.bistu.datacollectproofaudit.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.edu.bistu.datacollectproofaudit.pojo.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

/**
 * token工具类
 */

public class TokenUtils {

//    public static String getToken(User user) {
////        Date start = new Date();
////        long currentTime = System.currentTimeMillis() + 60 *60 * 1000;//6min有效时间
////        Date end = new Date(currentTime);
////        String token = "";
//////        token = JWT.create().withAudience(String.valueOf(user.getId())).withIssuedAt(start).withExpiresAt(end)
//////                .sign(Algorithm.HMAC256(user.getPassword()));
////
////        return token;
//
//    }

    public static final String SIGNATURE = "andrew";

    public static String createToken(HashMap<String,String> hashMap,User user){
        //设置一个时间，作为令牌的过期时间，设计时间为60分钟后
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND,2);

        //创建jwt builder
        JWTCreator.Builder builder = JWT.create();

        //遍历map集合,将信息放到payload的Claim中
        hashMap.forEach((k,v)-> builder.withClaim(k,v));

        String token = builder.withAudience(String.valueOf(user.getId())).withExpiresAt(calendar.getTime())
                .withSubject("subject")
                .sign(Algorithm.HMAC256(SIGNATURE));

        return token;
    }

    /**
     * 验证token  DecodedJWT 为解密之后的对象 可以获取payload中添加的数据
     * @param token
     * @return
     */
    public static DecodedJWT verifyToken(String token){
        //进行token的校验,注意使用同样的算法和同样的秘钥
        return JWT.require(Algorithm.HMAC256(SIGNATURE)).build().verify(token);
    }

    /**
     * 不验证token是否过期，获取token信息
     * @param token
     * @return
     */
    public static DecodedJWT decodeToken(String token){
        return JWT.decode(token);
    }



    public static Integer getTokenUserId() {
        String token = getRequest().getHeader("token");// 从 http 请求头中取出 token
        String userid = JWT.decode(token).getAudience().get(0);
        Integer userId = Integer.valueOf(userid);
        return userId;
    }

    /**
     * 获取request
     * @return
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        return requestAttributes == null ? null : requestAttributes.getRequest();
    }

}

