package com.edu.bistu.datacollectproofaudit.mapper;

import com.edu.bistu.datacollectproofaudit.pojo.UnstructureData;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UnstructureDataMapper {

    int insertUnstructureData(UnstructureData unstructureData);//插入一个或多个非结构化数据源 synchronizeState,int isdelete,String dataSourceName,int lsn,int displayNumber);//查询非结构化数据源数据

    List<UnstructureData> selectUnstructDatasource(String synchronizeState,String isdelete, String dataSourceName,int lsn,int displayNumber);//查询非结构化数据源数据

    Integer selectUnstructDatasourceNumber(String synchronizeState,String isdelete,String dataSourceName);//查询非结构化数据源数据数量

    UnstructureData  selectUnstructDatasourceByDbnameAndTablename(String dataSourceName,String dbname,String tablename);//根据数据库名和表名查看该数据源是否存在

    int updateStateBySchema(Integer id, Integer state);//同步状态设置为正在同步：1（根据表名新同步状态）根据表名

    int updateUnstructureDataById(UnstructureData unstructureData);//通过id更新非结构化数据源数据

    int isSynchronizeTableExist(String synchronizetable);//检验同步表是否存在,表名是“原数据库名+原表名”

    int updateSynctableField(String synchronizetablename,Integer id);//通过数据库名、表名和数据源名更新同步表名

    int addNewSynchronizeTable(String synchronizetable);//新建同步表

   // List<String> selectAllFieldName(String synchronizetable);//查询新建表中的所有字段名

    int alterNewField(String synchronizetable,String field,String fieldtype); //新表中添加数据源中待同步的字段

    int insertNewField(String synchronizetable,String field,String fieldvalue);//同步表中插入需要同步的字段值

    // int updateNewField(String synchronizetable,String field,String fieldvalue);//同步表中更新需要同步的字段值

    String  selectSynlistByDbnameAndTablename(Integer id);//根据数据库名和表名查看syn_list列

    int updatePositon(int id, String synposition);//根据表名更新同步位置

    String getSyncPosition(Integer id);//判断同步位置

   // List<UnstructureData> selectDatasourceByDataSourceName(String dataSourceName);//根据数据源名查询所有非结构化数据源数据

    UnstructureData selectUnstructDataDatasourceById(Integer id);//根据id查询非结构化数据源

    String getSyncTable(Integer id);//根据id查询同步表表名

    int updateIsdeleteById(Integer id);//根据Id更新isdelete

    List<Map<String,Object>> selectUnstructureDataBySynchronizetablename(String synchronizetable, int lsn, int displayNumber);//根据表名查询数据源对应的同步表中所有数据

    List<Map<String,Object>> selectSynchronizeTableData(String tablename, String keywords, String proofreader, int status, Date acquisitionTimeStart,Date acquisitionTimeEnd,Date proofreadingTimeStart,Date proofreadingTimeEnd, int lsn, int displayNumber,String user);//根据数据源名，关键字，操作人，校对状态，采集时间，校对时间，校对类型查询数据

    Integer selectSynchronizeTableDataNumber(String tablename, String keywords, String proofreader, int status,  Date acquisitionTimeStart,Date acquisitionTimeEnd,Date proofreadingTimeStart,Date proofreadingTimeEnd,String user);//根据数据源名，关键字，操作人，校对状态，采集时间，校对时间，校对类型查询数据数量

    int updateSynchronizeTableIsdeleteById(String tablename,Integer id);//根据ID更新同步表的isdelete

    int updateSynchronizeTableStatusIsproofAndIsreworkById(String tablename, Integer id, Timestamp proofTime,int status);//根据ID更新同步表的status

    int updateSynchronizeTableTimeById(String tablename, Integer id, Timestamp proofTime);//根据ID更新同步表的标注时间

    int updateSynchronizeTableStatusIsreviewAndIscallbackById(String tablename, Integer id,Timestamp reviewTime,String reviewer,int status);//根据ID更新同步表的status
    List<UnstructureData> selectUnstructureDatasourceName();//查询非结构化数据源中所有数据源

    UnstructureData selectUnstructDataDatasourceBydataSourceName(String dataSourceName);//根据数据源名查询非结构化数据源

    List<Map<String,Object>> selectUnassignedUnstructureDataBySynchronizetablename(String synchronizetable,int lsn,int displayNumber);//根据表名查询数据源对应的同步表中未分配的数据

    Integer selectUnassignedUnstructureDataNumber(String synchronizetable);//根据表名查询数据源对应的同步表中未分配的数据数量

    int updateUnstructureDatadistributionById(String tablename, String proofreader, Integer id,Timestamp scheduleTime);//更新同步表中的是否分配,校对人以及分配时间字段

    UnstructureData getSynList(Integer id);

    Map<String,Object> getEntityContent(Integer datasourceId, String tableName);//列表页进入详情页，获取当亲页面的内容

    int dropTableByname(String tablename);

    Integer getSyncState(Integer id);//获得同步状态

    Integer getSyncCycle(Integer id);//获得该数据源的同步周期（分钟）

    Integer getUndataNumber();//获取数据源数量

    //根据时间段和用户名获取用户工作情况
    Integer selectViewProofByPeriod(String tablename,String username,Date acquisitionTimeStart, Date acquisitionTimeEnd);

    Integer selectViewDisProof(String tablename,String username);//未标注数据情况不考虑时间段

    Integer selectViewCallback(String tablename,String username);//未标注数据情况不考虑时间段

    Integer selectViewRework(String tablename,String username);//未标注数据情况不考虑时间段

    Integer selectViewReviewByPeriod(String tablename,String username,Date acquisitionTimeStart, Date acquisitionTimeEnd);

    Integer selectViewProofByUsername(String tablename,String username);

    Integer selectViewReviewByUsername(String tablename,String username);

    List<Map<String,Object>> selectAllUnstrutrueDataByFourTimeAndUsername(String tablename,int status, String proofreader, String reviewer,String keywords,
                                                                          Date acquisitionTimeStart, Date acquisitionTimeEnd,//采集时间 update_time
                                                                          Date scheduleTimeStart,Date scheduleTimeEnd,//分配时间 schedule_time
                                                                          Date reviewTimeStart,Date reviewTimeEnd,//审核时间 rev_time
                                                                          Date proofreadingTimeStart,Date proofreadingTimeEnd,//标注时间 proof_time
                                                                          Integer lsn,Integer displayNumber);

    Integer selectAllUnstrutrueDataNumberByFourTimeAndUsername(String tablename,int status,String proofreader, String reviewer,String keywords,
                                                                       Date acquisitionTimeStart, Date acquisitionTimeEnd,//采集时间 update_time
                                                                       Date scheduleTimeStart,Date scheduleTimeEnd,//分配时间 schedule_time
                                                                       Date reviewTimeStart,Date reviewTimeEnd,//审核时间 rev_time
                                                                       Date proofreadingTimeStart,Date proofreadingTimeEnd);//标注时间 proof_time


    Integer getStatusByDataId(String tablename,int id);//更新同步表状态，同时更新校对完成时间、审核完成时间、审核人

    Integer changeTaskFlagStart(int id);

    Integer changeTaskFlagEnd(int id);

    String getUrlById(String tablename,String id);

    String getDataNameById(String id);


}