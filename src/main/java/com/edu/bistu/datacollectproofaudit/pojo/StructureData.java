package com.edu.bistu.datacollectproofaudit.pojo;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "structure_data")
public class StructureData {
    /**
     * 唯一标识
     */
    @Id
    private Integer id;

    /**
     * 数据源名
     */
    private String dataSourceName;

    /**
     * 数据源类型:
(mysql;oracel)
     */
    private String dbtype;

    /**
     * 地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 库名
     */
    private String dbname;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 表名
     */
    private String tablename;

    /**
     * 同步状态：
0：未启动
1：正在同步
2：同步完成
3：异常

     */
    private Integer synchronizeState;

    /**
     * 同步周期（分钟）
     */
    private Integer synchronizeCycle;

    /**
     * 同步位置（时间）
     */
    private Date synchronizePosition;

    /**
     * 同步列表
     */
    private String synList;

    /**
     * 默认是0：
0：未删除
1：已删除
     */
    private Integer isdelete;


    /**
     * 数据源类型
     */

    private Integer datatype;

    /**
     * 获取唯一标识
     *
     * @return id - 唯一标识
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置唯一标识
     *
     * @param id 唯一标识
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取数据源名
     *
     * @return data_source_name - 数据源名
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * 设置数据源名
     *
     * @param dataSourceName 数据源名
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * 获取数据源类型:
(mysql;oracel)
     *
     * @return dbtype - 数据源类型:
(mysql;oracel)
     */
    public String getDbtype() {
        return dbtype;
    }

    /**
     * 设置数据源类型:
(mysql;oracel)
     *
     * @param dbtype 数据源类型:
(mysql;oracel)
     */
    public void setDbtype(String dbtype) {
        this.dbtype = dbtype;
    }

    /**
     * 获取地址
     *
     * @return host - 地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置地址
     *
     * @param host 地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取端口
     *
     * @return port - 端口
     */
    public Integer getPort() {
        return port;
    }

    /**
     * 设置端口
     *
     * @param port 端口
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 获取库名
     *
     * @return dbname - 库名
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * 设置库名
     *
     * @param dbname 库名
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * 获取用户名
     *
     * @return username - 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取表名
     *
     * @return tablename - 表名
     */
    public String getTablename() {
        return tablename;
    }

    /**
     * 设置表名
     *
     * @param tablename 表名
     */
    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    /**
     * 获取同步状态：
0：未启动
1：正在同步
2：同步完成
3：异常

     *
     * @return synchronize_state - 同步状态：
0：未启动
1：正在同步
2：同步完成
3：异常

     */
    public Integer getSynchronizeState() {
        return synchronizeState;
    }

    /**
     * 设置同步状态：
0：未启动
1：正在同步
2：同步完成
3：异常

     *
     * @param synchronizeState 同步状态：
0：未启动
1：正在同步
2：同步完成
3：异常

     */
    public void setSynchronizeState(Integer synchronizeState) {
        this.synchronizeState = synchronizeState;
    }

    /**
     * 获取同步周期（分钟）
     *
     * @return synchronize_cycle - 同步周期（分钟）
     */
    public Integer getSynchronizeCycle() {
        return synchronizeCycle;
    }

    /**
     * 设置同步周期（分钟）
     *
     * @param synchronizeCycle 同步周期（分钟）
     */
    public void setSynchronizeCycle(Integer synchronizeCycle) {
        this.synchronizeCycle = synchronizeCycle;
    }

    /**
     * 获取同步位置（时间）
     *
     * @return synchronize_position - 同步位置（时间）
     */
    public Date getSynchronizePosition() {
        return synchronizePosition;
    }

    /**
     * 设置同步位置（时间）
     *
     * @param synchronizePosition 同步位置（时间）
     */
    public void setSynchronizePosition(Date synchronizePosition) {
        this.synchronizePosition = synchronizePosition;
    }

    /**
     * 获取同步列表
     *
     * @return syn_list - 同步列表
     */
    public String getSynList() {
        return synList;
    }

    /**
     * 设置同步列表
     *
     * @param synList 同步列表
     */
    public void setSynList(String synList) {
        this.synList = synList;
    }

    /**
     * 获取默认是0：
0：未删除
1：已删除
     *
     * @return isdelete - 默认是0：
0：未删除
1：已删除
     */
    public Integer getIsdelete() {
        return isdelete;
    }

    /**
     * 设置默认是0：
0：未删除
1：已删除
     *
     * @param isdelete 默认是0：
0：未删除
1：已删除
     */
    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }


    public Integer getDatatype() {
        return datatype;
    }

    public void setDatatype(Integer datatype) {
        this.datatype = datatype;
    }
}