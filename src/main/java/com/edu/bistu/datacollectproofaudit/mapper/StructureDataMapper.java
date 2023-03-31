package com.edu.bistu.datacollectproofaudit.mapper;

import com.edu.bistu.datacollectproofaudit.pojo.StructureData;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface StructureDataMapper{

    //int insertStructureData(StructureData structureData);//插入一个或多个结构化数据源

    List<StructureData> selectStructDatasource(String synchronizeState,String isdelete,String dataSourceName,int lsn,int displayNumber);//查询结构化数据源数据

    Integer selectStructDatasourceNumber(String synchronizeState,String isdelete,String dataSourceName);//查询结构化数据源数据数量

    int updateIsdeleteById(Integer id);//根据Id更新isdelete

    StructureData selecStructDataDatasourceById(Integer id);//根据id查询结构化数据源

    int updateStructureDataById(StructureData structureData);//通过id更新结构化数据源数据

    List<StructureData> selecStructureDatasourceName();
}