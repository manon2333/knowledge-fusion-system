package com.edu.bistu.datacollectproofaudit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 数据源工厂类
 */
public class DataSourceFactory {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFactory.class);

    public synchronized static DataSource getDataSource(DBType dbtype, String host, String port, String dbname, String user, String password){
        String driver;
        String url;
        switch (dbtype){
            case mysql:
                driver="com.mysql.cj.jdbc.Driver";
                url="jdbc:mysql://"+host+":"+port+"/"+dbname+"?useUnicode=true&characterEncoding=utf-8";
                break;
            case oracle:
                driver="oracle.jdbc.driver.OracleDriver";
                url= "jdbc:oracle:thin:@"+host+":"+port+":"+dbname;
                break;
            case PostgreSQL:
                driver="org.postgresql.Driver";
                url="jdbc:postgresql://"+host+":"+port+'/'+dbname;
                break;
            case DB2:
                driver="com.ibm.db2.jdbc.net.DB2Driver";
                url="jdbc:db2:"+host+":"+port+dbname;
                break;
            case Sybase:
                driver="com.sybase.jdbc.SybDriver";
                url="jdbc:sybase:Tds:"+host+": "+port+"/"+dbname;
                break;
            default:
                log.error("目前不支持的数据库类型：[{}]", dbtype);
                return null;
        }
        return linkDataSource(driver, url, user, password);
    }

    public synchronized static DriverManagerDataSource linkDataSource(String driver, String url, String user, String password){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }


    public enum DBType{
        mysql, oracle, PostgreSQL, DB2, Sybase, JSON, DOCSs
    }
}
