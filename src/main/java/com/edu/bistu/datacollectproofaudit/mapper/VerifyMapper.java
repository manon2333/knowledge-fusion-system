package com.edu.bistu.datacollectproofaudit.mapper;

import com.edu.bistu.datacollectproofaudit.pojo.Verify;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface VerifyMapper{

    int checkTriple(Verify verify);//判断三元组在表中是否存在

    int checkTypeTriple(Verify verify);//判断类型三元组在表中是否存在

    int checkBinary(String subject, String predicate);//判断二元组在表中是否存在（主语和谓语）

    int insertTriplet(Verify verify);//插入三元组

    Verify selectByPrimaryKey(Integer id);//根据ID查询三元组

    Verify getEntityByNameAndCategory(String datasourceId,String entityName,String categoryName);//根据实体名和实体类型检索实体类型三元组

    Verify getEntityByNameAndDatasourceId(String datasourceId,String entityName);//根据实体名和页面ID检索实体类型三元组

    int updateTriplet(Verify verify);//更新三元组

    List<Verify>  getEntityByName(String datasourceId,String entityName);//根据实体名检索实体

    List<Verify> getEntityByType(String datasourceId, String entityType);//得到该页面某种类型的实体

    List<Verify> getEntity(String datasourceId, String entityType,String entityName);//根据名称和类型检索实体

    List<Verify> fuzzyQueryEntityname(String entityName);//模糊查询实体名

    List<Verify> fuzzyQueryEntityAlias(String entityName);//模糊查询实体别名

    List<Verify>  selectEntityByEntityType(String entitytype);//根据实体类型查询实体名

    List<Verify> queryEntityByHeadname(String entityName , String datasourceId);//查询头实体为该实体名的实体

    List<Verify> queryEntityByTailname(String entityName, String datasourceId);//查询尾实体为该实体名的实体

    int deleteEntity(String entityName);//删除包含该实体的所有三元组

    int deleteTriplet(Integer id); //根据id删除三元组

    int changeStatus(String id,String status);//根据id通过三元组

//    List<String> getAllEntity(String datasourceId);//得到该页面所有的实体

    List<Verify> getAllEntity(String datasourceId);//得到该页面所有的实体


    List<Verify> selectAttributeTripleByPredicateType(String entity,String datasourceId);//根据谓语类型查询对应实体属性三元组

    List<Verify> selectRelationTripleByPredicateType(String entity,String datasourceId);//根据谓语类型查询对应实体关系三元组

    int delTriplet(String subject, String predicate); //删除三元组

    int getIsProof(String tableName, Integer id);//查看该html页是否校对

    String getEntityType(String subject,String datasourceId);//查看实体的类型

    List<Map<String,String>> getProperties(String subject);//得到属性某实体属性名和属性值的map

    List<String> checkEntityType(String datasourceid);//查看数据源下的实体类型

    int updateObject(String subject, String object);//更改实体类型

    int selectTheWholeEntityStatus(String subject,String datasourceid);

}