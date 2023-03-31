package com.edu.bistu.datacollectproofaudit.mapper;

import com.edu.bistu.datacollectproofaudit.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper{

    List<User> selectAllUsernameAs(Integer lsn,Integer displayNumber);//查询所有标注人员用户名

    List<User> selectAllUsername();//查询所有标注人员用户名

    List<User> selectAllManagerName();//查询所有管理人员

    String findUsernameById(String id);//根据id获取用户名

    String findManagerById(String id);//根据id获取管理员名

    int getUserNumber();//查询标注人员数量

    int isExist(String username);

    User getDetailMessage(String username);//根据标注人员用户名获取其全部信息

    boolean login(String username, String password);//标注人员登陆验证账号和密码

    boolean loginManager(String username, String password);//管理人员登陆验证账号和密码

    User findUserById(int id);

    User findManagerNameById(int id);

    User findUserByName(String username);

    User findManagerByName(String username);

    String findUserByKeyword(String namekeyword);




}