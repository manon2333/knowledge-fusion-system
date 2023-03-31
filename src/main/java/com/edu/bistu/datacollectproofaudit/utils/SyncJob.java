package com.edu.bistu.datacollectproofaudit.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.edu.bistu.datacollectproofaudit.pojo.Field;
import com.edu.bistu.datacollectproofaudit.service.DataSourceFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@EnableScheduling
public class SyncJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SyncJob.class);



    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String tableName = (String)dataMap.get("tableName");//爬虫库的表名
        String syncListStr = (String)dataMap.get("syncListStr");//爬虫库的同步信息
        String syncTable = (String) dataMap.get("syncTable");//对应本地库的同步表
        String dbType = (String) dataMap.get("dbType");//爬虫库数据类型
        String host = (String) dataMap.get("host");//爬虫库的host
        int port = (int)dataMap.get("port");//爬虫库的port
        String dbName = (String) dataMap.get("dbName");//爬虫库的数据库名
        String userName = (String) dataMap.get("userName");//爬虫库的username
        String password = (String) dataMap.get("password");//爬虫图的密码
        int id = (int) dataMap.get("id");//爬虫库信息所对应的id
        String localUrl = (String) dataMap.get("localUrl");//本地的数据库的url
        String localUserName = (String) dataMap.get("localUserName");//本地的数据库的username
        String localPassword = (String) dataMap.get("localPassword");//本地的数据库的password
        try {
            startAutoSyncJob(tableName, syncListStr, syncTable, dbType, host, port, dbName, userName, password, id, localUrl, localUserName, localPassword);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        System.out.println("同步数据库开始执行");
    }



    public void startAutoSyncJob(String tableName, String syncListStr, String syncTable, String dbType, String host, int port, String dbName,
                                 String userName, String password, int id, String localUrl, String localUserName, String localPassword) throws SQLException {
        String field;
        String isdatasource;
        String dataSource = null;
        String filter = null;
        String update_time = null;//同步时间
        String sql;
        //从非结构数据源中读取synlist中的同步字段信息
        List<Field> synli = JSON.parseObject(syncListStr, new TypeReference<ArrayList<Field>>() {});
        if (syncTable==null || syncTable.equals("")){
            log.info("该同步表不存在！");
        }
//        DataSource ds1 = DataSourceFactory.getDataSource(DataSourceFactory.DBType.valueOf("mysql"),
//                "localhost", "3306", "proof", "root", "12345678");
//        assert ds1 != null;
//        Connection conn1 = ds1.getConnection();
        try {
            //TODO 目前只支持MySQL数据库，未来可以添加Oracle等其他数据库
            String driver = "com.mysql.cj.jdbc.Driver";
            DataSource ds1 = DataSourceFactory.linkDataSource(driver, localUrl, localUserName, localPassword);
            Connection conn1 = ds1.getConnection();
            Statement stmt1 = conn1.createStatement();
            //查看上次同步位置
            String sql0 = String.format("select synchronize_position from unstructure_data where id = '%d' limit 1;", id);
            ResultSet rs = stmt1.executeQuery(sql0);
            String syncPosition = "";
            while (rs.next()) {
                syncPosition = rs.getString("synchronize_position");
            }
            //读取过滤条件和时间内戳字段，将除过滤条件字段的其他字段放入一个list并转为用“,"分开的字符串
            List<String> fieList = new ArrayList<>();
            Map<String, String> map = new HashMap<>();//<旧字段名，新字段名>，用于同步时字段值映射到固定的字段（title，content，update_time）中
            for (Field f : synli) {
                field = f.getField();
                String isUpdateTime = f.getUpdatetime();
                String isTitle = f.getTitle();
                String isContent = f.getHtml();
                isdatasource = f.getIsdatasource();
                if (isdatasource.equals("1")) {
                    filter = field;
                    dataSource = f.getDatasource();
                } else {
                    if (isUpdateTime.equals("1")) {
                        update_time = field;
                        if (!f.getFieldtype().equals("timestamp")) {
                            //把2019-06-08 00:00:00格式改为  2019/6/8 00:00:00
                            syncPosition = syncPosition.replace("-", "/").replace("/0", "/");
                            log.info("同步位置" + ":" + syncPosition);
                        }
                        if (!field.equals("update_time")) {
                            map.put(update_time, "update_time");
                        }
                    } else if (isTitle.equals("1") && !field.equals("title")) {
                        map.put(field, "title");
                    } else if (isContent.equals("1") && !field.equals("content")) {
                        map.put(field, "content");
                    }
                    fieList.add(field);
                }
            }
            String fieldStr = String.join(",", fieList);
            if (filter != null) {//存在datasource列时
                sql = "select " + fieldStr + " from " + tableName + " where " + filter + " like '%" + dataSource + "%'" + "and " + update_time + ">" + "'" + syncPosition + "'" + "order by " + update_time + " asc";
            } else {//不存在datasource列时
                sql = "select " + fieldStr + " from " + tableName + " where " + update_time + ">" + "'" + syncPosition + "'" + "order by " + update_time + " asc";
            }
            //将对应字段源数据同步到新的表中
            syncData(sql, fieList, syncTable, update_time, stmt1, dbType, host, port, dbName, userName, password, id, map);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /**
     * 从爬虫库中读取数据并插入新库
     */
    public void syncData(String sql, List<String> fielist, String syncTable, String time, Statement stmt1,
                              String dbType, String host, int port, String dbName, String userName, String password, int id, Map<String,String> map) {
        Connection conn;
        try {
            DataSource ds = DataSourceFactory.getDataSource(DataSourceFactory.DBType.valueOf(dbType),
                    host, String.valueOf(port), dbName, userName, password);
            if (ds==null){
                log.error("无法获取数据库连接");
                return;
            }
            conn = ds.getConnection();

            if(conn==null){
                log.error("无法获取数据库连接");
                return;
            }
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            List<Map<String,String>> allfieldli=new ArrayList<>();
            while (rs.next()) {
                Map<String,String> fieldmap = new HashMap<>();
                for(String fie:fielist){
                    String fieldvalue=rs.getString(fie);
                    fieldmap.put(fie,fieldvalue);
                }
                allfieldli.add(fieldmap);
            }
            log.info("从爬虫库读取数据成功！");
            //将同步状态置为 1: 正在同步
            String sql0 = String.format( "update unstructure_data set synchronize_state = '1' where id = %d", id);
            stmt1.executeUpdate(sql0);
            //插入新表
            for(Map<String ,String> fiemap:allfieldli){
                StringBuilder fieKey= new StringBuilder();
                StringBuilder fieValue= new StringBuilder();
                List<String> keys = new ArrayList<>(fiemap.keySet());
                for(String ma : map.keySet()){
                    int i = keys.indexOf(ma);
                    keys.set(i, map.get(ma));
                }
                for (String key:keys){
                    fieKey.append(key).append(",");
                }
                fieKey = new StringBuilder(fieKey.substring(0, fieKey.length() - 1));
                Collection<String> values = fiemap.values();
                for (String value:values){
                    if (value==null){
                        continue;
                    }
                    fieValue.append("'").append(value.replace("'", "\\'").trim()).append("'").append(",");
                }
                fieValue = new StringBuilder(fieValue.substring(0, fieValue.length() - 1));
                String sql1 = String.format( "insert into %s (%s) values(%s);", syncTable, fieKey.toString(), fieValue.toString());
                stmt1.executeUpdate(sql1);
                String synposition=fiemap.get(time);
                String sql2 = String.format( "update unstructure_data set synchronize_position = '%s' where id = %d;", synposition, id);
                stmt1.executeUpdate(sql2);
            }
            //将同步状态置为 4: 定时同步同步后的等待下次同步状态
            String sql3 = String.format( "update unstructure_data set synchronize_state = '4' where id = %d", id);
            stmt1.executeUpdate(sql3);
            log.info("同步表同步成功！");
        }catch (SQLException e){
            e.printStackTrace();
            log.info("数据库连接错误,错误信息：",e);
        }
    }


}
