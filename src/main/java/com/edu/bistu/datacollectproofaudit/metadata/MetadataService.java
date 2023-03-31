package com.edu.bistu.datacollectproofaudit.metadata;

import com.edu.bistu.datacollectproofaudit.config.SysConfig;
import com.edu.bistu.datacollectproofaudit.utils.FileUtils;
import com.edu.bistu.datacollectproofaudit.utils.StringUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.relation.Relation;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author wuyu
 * @create 2022/7/20 17:20
 */
@Component
public class MetadataService {

    private static final Logger log = LoggerFactory.getLogger(MetadataService.class);

    private final SysConfig sysConfig;

    private static final Map<String, DataType> DATATYPE_MAP = new HashMap<>();

    static {
        DATATYPE_MAP.put("数值", DataType.NUMBER);
        DATATYPE_MAP.put("字符串", DataType.STRING);
        DATATYPE_MAP.put("日期", DataType.DATE);
        DATATYPE_MAP.put("图片", DataType.PICTURE);
    }

    private TreeType tree = null;

    //元数据文件的IRI
    private String iri = "";

    //顶部owl:thing节点
    private EntityType topEntityType = new EntityType();

    //顶部节点name
    private static final String topEntityName = "owl:thing";

    //初始化两个map列表用于实体name和id映射
    private Map<String, String> entityIdToName = new HashMap<>();
    private Map<String, String> entityNameToId = new HashMap<>();


    /**
     * 到指定目录下，根据预定义的命名规则，
     * 查找最新版本的元数据文件，并将它的文件名返回。
     *
     * @return 如果找到了最新版本的元数据文件，则返回文件名，否则返回null
     */
    public String getLatestMetafile() {
        //指定目录为owl，元数据文件的命名格式为newmeta_{version}.owl
        List<String> files = FileUtils.getFiles(this.sysConfig.getMetadata());
        if (files == null || files.isEmpty()) {
            return null;
        }
        String pattern = "newmeta_(\\d+)\\.owl";
        Pattern pat = Pattern.compile(pattern);
        int latest = 0;
        String file = null;
        for (String name : files) {
            Matcher mat = pat.matcher(name);
            if (mat.find()) {
                int version = Integer.parseInt(mat.group(1));
                if (version > latest) {
                    latest = version;
                    file = name;
                }
            }
        }
        if (latest != 0) {
            return file;
        }
        log.error("指定目录下找不到匹配命名规范的元数据文件");
        return null;
    }

    public MetadataService(@Autowired SysConfig sysConfig) throws Exception {
        this.sysConfig = sysConfig;
        //尝试加载元数据文件，尝试找最新版本的元数据文件
        String file = getLatestMetafile();
        if (file == null) {
            throw new Exception(String.format("[%s]目录下名为newmeta_{version}.owl的元数据文件不存在", this.sysConfig.getMetadata()));
        }
        if (reload(file)) {
            log.info("加载元数据文件成功，其实体类型[{}]种，属性[{}]种，关系[{}]种", getAllLeafEntityTypes().size(), getAllProperty().size(), getAllRelation().size());
        } else {
            throw new Exception(String.format("无法加载元数据文件[%s]", file));
        }
    }

    /**
     * 【标注系统接口】
     * 获取所有叶子节点
     *
     * @return 系统所有叶子节点名称清单
     */
    public List<Map<String, String>> getAllLeafNodeName() {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            Map<String, String> leafName = this.tree.getLeafName();
            for (String id : leafName.keySet()) {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("name", leafName.get(id));
                results.add(map);
            }
            return results;
        } catch (Exception e) {
            log.error("获取所有叶子节点失败");
            return Collections.emptyList();
        }
    }


    /**
     * 【标注系统接口】
     * 根据实体id，获取该实体的属性列表
     *
     * @param entityId 实体类型id
     * @return 返回实体的属性清单
     */
    public List<PropertyType> getProperties(String entityId) {
        return tree.getProperties(entityId);
    }


    /**
     * 【标注系统接口】
     * 根据实体类型，获取该实体的domain关系列表
     *
     * @param entityId 实体id
     * @return 返回系统中实体的domain关系清单
     */
    public List<RelationType> getDomainRelations(String entityId) {
        try {
            List<RelationType> relations = this.tree.getRelations(entityId);
            List<RelationType> domainRelation = new ArrayList<>();
            List<String> superTypeById = getSuperTypeById(entityId);
            for (RelationType relation : relations) {
                if (superTypeById != null && (superTypeById.contains(relation.getDomain()) || StringUtil.strEquals(relation.getDomain(), getEntityNameById(entityId)))) {
                    domainRelation.add(relation);
                } else if (superTypeById == null && StringUtil.strEquals(relation.getDomain(), getEntityNameById(entityId))) {
                    domainRelation.add(relation);
                }
            }
            return domainRelation;
        } catch (Exception e) {
            log.error("获取实体[{}]的domain关系列表失败", entityId);
            return Collections.emptyList();
        }
    }


    /**
     * 【标注系统接口】
     * 根据实体类型，获取该实体的range关系列表
     *
     * @param entityId 实体id
     * @return 返回系统中实体的range关系清单
     */
    public List<RelationType> getRangeRelations(String entityId) {
        try {
            List<RelationType> relations = this.tree.getRelations(entityId);
            List<RelationType> rangeRelation = new ArrayList<>();
            List<String> superTypeById = getSuperTypeById(entityId);
            for (RelationType relation : relations) {
                if (superTypeById != null && (superTypeById.contains(relation.getRange()) || StringUtil.strEquals(relation.getRange(), getEntityNameById(entityId)))) {
                    rangeRelation.add(relation);
                } else if (superTypeById == null && StringUtil.strEquals(relation.getRange(), getEntityNameById(entityId))) {
                    rangeRelation.add(relation);
                }
            }
            return rangeRelation;
        } catch (Exception e) {
            log.error("获取实体[{}]的range关系列表失败", entityId);
            return Collections.emptyList();
        }
    }

    /**
     * 【标注系统接口】
     * 根据一个节点的名字返回兄弟节点名称，包含自身
     *
     * @param entityId 实体id
     * @return 包含自身的兄弟节点名称清单
     */
    public List<Map<String, String>> getBrothers(String entityId) {
        try {
            if (this.tree.getNodeMap().containsKey(entityId)) {
                List<String> superTypes = this.tree.getNodeMap().get(entityId).getSuperTypes();
                EntityType entityType = this.tree.getNodeMap().get(topEntityName);
                List<String> children = entityType.getChildren();
                if (superTypes != null && superTypes.size() != 0) {
                    for (String superType : superTypes) {
                        if (children.contains(superType)) {
                            List<Map<String, String>> results = getAllSubclass(superType);
                            return results;
                        }
                    }
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("根据实体id:[{}]返回兄弟节点失败", entityId);
            return Collections.emptyList();
        }
    }


    /**
     * 根据实体id，获取该实体的祖先类列表
     *
     * @param entityId 实体id
     * @return 祖先类列表
     */
    public List<String> getSuperTypeById(String entityId) {
        if (this.tree.getNodeMap().containsKey(entityId)) {
            EntityType entityType = this.tree.getNodeMap().get(entityId);
            return entityType.getSuperTypes();
        }
        return Collections.emptyList();
    }

    /**
     * 【标注系统接口】
     * 根据实体id获取实体名称
     *
     * @param entityId 实体id
     * @return 实体id
     */
    public String getEntityNameById(String entityId) {
        if (entityIdToName.containsKey(entityId)) {
            return entityIdToName.get(entityId);
        }
        return null;
    }


    /**
     * 【标注系统接口】
     * 获取entityId下所有子类叶子节点(若存在非叶子节点,递归获取其叶子节点)
     *
     * @param entityName 实体名称
     * @return 其下所有子类叶子节点
     */
    public List<Map<String, String>> getAllSubclass(String entityName) {
        String entityId = entityNameToId.get(entityName);
        List<Map<String, String>> result = new ArrayList<>();
        EntityType entity = this.tree.getNodeMap().get(entityId);
        List<String> children = entity.getChildren();
        if (children != null && children.size() != 0) {
            for (String child : children) {
                Map<String, String> map = new HashMap<>();
                if (entityNameToId.containsKey(child) && !this.tree.getNodeMap().get(entityNameToId.get(child)).isLeaf()) {
                    List<Map<String, String>> partResult = getAllSubclass(child);
                    result.addAll(partResult);
                } else {
                    map.put("id", entityNameToId.get(child));
                    map.put("name", child);
                    result.add(map);
                }
            }
        }
        return result;
    }


    /**
     * 返回当前实体名称下所有直系子类节点
     *
     * @param entityType 实体类型名称
     * @return 实体的所有直系子类节点清单
     */
    public List<String> getAllChildren(String entityType) {
        return tree.getAllChildren(entityType);
    }


    /**
     * 获取系统中配置的所有实体类型
     */
    public List<EntityType> getAllLeafEntityTypes() {
        return tree.getLeafNodes();
    }


    /**
     * 根据属性名称,获取该属性的单位
     *
     * @param propertyName 属性名称
     * @return 返回名称为propertyName的属性定义
     */
    public List<String> getPropertyDefine(String propertyName) {
        return this.tree.getPropertyDefine(propertyName);
    }


    /**
     * 获取entityIdToName
     */
    public Map<String, String> getEntityIdToName() {
        return entityIdToName;
    }

    /**
     * 获取entityNameToId
     */
    public Map<String, String> getEntityNameToId() {
        return entityNameToId;
    }


    /**
     * 返回当前实体名称下所有直系子类节点
     *
     * @param entityType 实体类型名称
     * @return
     */
    public List<String> getAllLinealSubclass(String entityType) {
        return this.tree.getAllChildren(entityType);
    }


    /**
     * 获取所有的属性
     *
     * @return 元数据文件中所有的属性（没有去重）
     */
    public Map<String, PropertyType> getAllProperty() {
        return this.tree.getAllProperties();
    }

    /**
     * 获取所有的关系
     *
     * @return 元数据文件中所有的关系（没有去重）
     */
    public Map<String, RelationType> getAllRelation() {
        return this.tree.getAllRelations();
    }


    /**
     * (重新加载元数据owl文件),对数据的合法性进行检查,填充/更新entities数据结构
     *
     * @param file 需要加载的元数据文件名
     * @return 加载是否成功
     */
    private boolean reload(String file) throws FileNotFoundException, OWLOntologyCreationException {
        try {
            if (FileUtils.isFileExists(sysConfig.getMetadata(), file)) {
                //默认的树根为“实体类”
                this.tree = new TreeType("root");
                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(this.sysConfig.getMetadata(), file));
                this.tree = buildTree(ontology, "root", manager);

                //初始化id和实体name映射表
                for (String key : this.tree.getNodeMap().keySet()) {
                    String name = this.tree.getNodeMap().get(key).getName();
                    entityIdToName.put(key, name);
                    entityNameToId.put(name, key);
                }

                return true;
            } else {
                log.error("无法找到指定的owl文件:{}/{}", sysConfig.getMetadata(), file);
                return false;
            }
        } catch (Exception e) {
            log.error("无法加载元数据文件[{}]", e.getMessage());
            return false;
        }
    }

    /**
     * 在本体模型中，构建一颗以root为根的类型树
     *
     * @param ontology 本体模型
     * @param root     树根的实体的类名
     * @param manager  owl管理类
     * @return 构建完成的类型树
     */
    private TreeType buildTree(OWLOntology ontology, String root, OWLOntologyManager manager) {

        TreeType tree = new TreeType(root);

        //开始解析本体模型，构造本体树
        Set<OWLClass> classes = ontology.getClassesInSignature(); //本体模型中的类
        if (!classes.isEmpty()) {
            OWLClass firstClass = classes.iterator().next();
            String uriString = firstClass.getIRI().toString();
            //赋值iri
            iri = uriString.substring(0, uriString.indexOf("#") + 1);
        }

        //设置owl:thing类,所有大类的父类
        Map<String, EntityType> nodeMap = new HashMap<>();
        List<String> superType = new ArrayList<>();
        topEntityType = new EntityType(topEntityName, null);
        //获取大类,添加child
        for (OWLClass aClass : classes) {
            if (!isOWnSuperClass(aClass, ontology)) {
                superType.add(aClass.getIRI().getShortForm());
            }
        }
        topEntityType.setChildren(superType);
        nodeMap.put(topEntityName, topEntityType);
        tree.setNodeMap(nodeMap);

        //遍历本体模型中的类,填充本体树
        for (OWLClass cls : classes) {
            String clsName = cls.getIRI().getShortForm();
            String uuid = UUID.randomUUID().toString();
            nodeMap.put(uuid, clsToEntityType(cls, uuid, ontology, manager));
        }

        return tree;

    }


    /**
     * 由类名获取对应类
     *
     * @param clsName 类名
     * @param manager owl管理类
     * @return 类
     */
    private OWLClass clsNameToClass(String clsName, OWLOntologyManager manager) {
        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        //由类名获取类
        OWLClass owlClass = owlDataFactory.getOWLClass(iri, clsName);
        return owlClass;
    }

    /**
     * 由属性名获取对应属性
     *
     * @param propName 属性名
     * @param manager  owl管理类
     * @return 属性
     */
    private OWLDataProperty propNameToProperty(String propName, OWLOntologyManager manager) {
        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLDataProperty owlDataProperty = owlDataFactory.getOWLDataProperty(iri, propName);
        return owlDataProperty;
    }

    /**
     * 由关系名获取对应关系
     *
     * @param relationName 关系名
     * @param manager      owl管理类
     * @return 关系
     */
    private OWLObjectProperty relationNameToRelation(String relationName, OWLOntologyManager manager) {
        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLObjectProperty owlObjectProperty = owlDataFactory.getOWLObjectProperty(iri, relationName);
        return owlObjectProperty;
    }

    /**
     * 获取本体中某一个类的祖先父类
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 祖先父类
     */
    private List<String> getAllSuperClass(OWLClass cls, OWLOntology ontology, OWLOntologyManager manager) {

        List<String> allSuperClass = new ArrayList<>();
        String superClass = getSuperClass(cls, ontology);
        if (superClass != null) {
            //具有父类
            allSuperClass.add(superClass);
            OWLClass owlClass = clsNameToClass(superClass, manager);
            List<String> getSuperClass = getAllSuperClass(owlClass, ontology, manager);
            if (getSuperClass != null) {
                allSuperClass.addAll(getSuperClass);
            }
            return allSuperClass;
        }
        return null;
    }

    /**
     * 获取本体中某一个类的直接父类
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 直接父类
     */
    private String getSuperClass(OWLClass cls, OWLOntology ontology) {

        String superClass = "";
        if (ontology.getSubClassAxiomsForSubClass(cls).size() != 0) {
            for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSubClass(cls)) {
                String superClassString = sub.getSuperClass().toString();
                superClass = superClassString.substring(superClassString.indexOf('#') + 1, superClassString.length() - 1);
            }
            return superClass;
        }
        return null;
    }

    /**
     * 获取本体中某一个类的直接子类
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 直接子类
     */
    private List<String> getSubClass(OWLClass cls, OWLOntology ontology) {

        List<String> subClassList = new ArrayList<>();
        if (ontology.getSubClassAxiomsForSuperClass(cls).size() != 0) {
            for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(cls)) {
                String subClassString = sub.getSubClass().toString();
                String subClass = subClassString.substring(subClassString.indexOf('#') + 1, subClassString.length() - 1);
                subClassList.add(subClass);
            }
            return subClassList;
        }
        return null;
    }

    /**
     * 判断该实体是否有父类实体
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 是否具有父类实体
     */
    private boolean isOWnSuperClass(OWLClass cls, OWLOntology ontology) {
        String superClass = getSuperClass(cls, ontology);
        return superClass != null;
    }

    /**
     * 判断该实体是否为叶子节点实体
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 是否为叶子节点实体
     */
    private boolean isLeafClass(OWLClass cls, OWLOntology ontology) {
        List<String> subClass = getSubClass(cls, ontology);
        return subClass.size() == 0;
    }


    /**
     * 获取本体中某一个类关联的所有关系
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 类的关系列表
     */
    private List<RelationType> getRelationByClass(OWLClass cls, OWLOntology ontology) {

        List<RelationType> relationTypeList = new ArrayList<>();
        //类名称
        String clsName = cls.getIRI().getShortForm();

        //以该类为domain的关系
        for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
            if (op.getDomain().equals(cls)) {
                for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                    RelationType relationType = new RelationType();
                    Set<OWLObjectPropertyRangeAxiom> objectPropertyRangeAxioms = ontology.getObjectPropertyRangeAxioms(oop);
                    //关系名称
                    relationType.setName(oop.getIRI().getShortForm());
                    //关系domain名称
                    relationType.setDomain(clsName);
                    if (objectPropertyRangeAxioms.size() == 1) {
                        for (OWLObjectPropertyRangeAxiom opr : objectPropertyRangeAxioms) {
                            String rangeString = opr.getRange().toString();
                            String rs = rangeString.substring(rangeString.indexOf('#') + 1, rangeString.length() - 1);
                            //关系range名称
                            relationType.setRange(rs);
                        }
                        relationTypeList.add(relationType);
                    } else {
                        log.error("关系[{}]的Range只能有一个！", oop.getIRI().getShortForm());
                    }
                }
            }
        }

        //以该类为range的关系
        for (OWLObjectPropertyRangeAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
            if (op.getRange().equals(cls)) {
                for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                    Set<OWLObjectPropertyDomainAxiom> objectPropertyDomainAxioms = ontology.getObjectPropertyDomainAxioms(oop);
                    for (OWLObjectPropertyDomainAxiom opda : objectPropertyDomainAxioms) {
                        RelationType relationType = new RelationType();
                        //关系名称
                        relationType.setName(oop.getIRI().getShortForm());
                        //关系domain
                        String domainString = opda.getDomain().toString();
                        String ds = domainString.substring(domainString.indexOf('#') + 1, domainString.length() - 1);
                        relationType.setDomain(ds);
                        //关系range
                        relationType.setRange(clsName);
                        relationTypeList.add(relationType);
                    }

                }
            }
        }

        return relationTypeList;
    }


    /**
     * 获取本体中某一个类关联的所有属性
     *
     * @param cls      本体类
     * @param ontology 本体模型
     * @return 类的属性列表
     */
    private List<PropertyType> getPropertiesByClass(OWLClass cls, OWLOntology ontology) {

        List<PropertyType> propertyTypeList = new ArrayList<>();
        //类名称
        String clsName = cls.getIRI().getShortForm();

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLAnnotationProperty rdfsComment = manager.getOWLDataFactory().getRDFSComment();
        OWLAnnotationProperty owlVersionInfo = manager.getOWLDataFactory().getOWLVersionInfo();
        OWLAnnotationProperty rdfsIsDefinedBy = manager.getOWLDataFactory().getRDFSIsDefinedBy();
        for (OWLDataPropertyDomainAxiom dp : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
            if (dp.getDomain().equals(cls)) {
                for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
                    PropertyType propertyType = new PropertyType();
                    //属性名称
                    String propName = odp.getIRI().getShortForm();
                    //添加属性名称
                    propertyType.setName(propName);
                    //添加属性domain
                    propertyType.setDomain(clsName);
                    //属性注释信息
                    for (OWLAnnotationAssertionAxiom axiom : ontology.getAnnotationAssertionAxioms(odp.getIRI())) {
                        //设置属性类型
                        if (axiom.getProperty().equals(rdfsComment)) {
                            String annotation = axiom.getValue().toString();
                            String dataTypeStr = annotation.substring(1, annotation.length() - 13);
                            if (DATATYPE_MAP.containsKey(dataTypeStr)) {
                                //添加属性类型
                                propertyType.setDataType(DATATYPE_MAP.get(dataTypeStr));
                            } else {
                                log.error("属性[{}]的类型只能为(数值，字符串，日期，图片)！", propName);
                            }
                        }
                        //设置属性限定符
                        if (axiom.getProperty().equals(owlVersionInfo)) {
                            String qualifier = axiom.getValue().toString();
                            String qStr = qualifier.substring(1, qualifier.length() - 13);
                            propertyType.setQualifier(qStr);
                        }
                        //设置属性是否是一个范围
                        try {
                            if (axiom.getProperty().equals(rdfsIsDefinedBy)) {
                                String allowRString = axiom.getValue().toString();
                                Boolean allowStr = Boolean.valueOf(allowRString.substring(1, allowRString.length() - 14));
                                propertyType.setAllowRangeValue(allowStr);
                            } else {
                                propertyType.setAllowRangeValue(false);
                            }
                        } catch (Exception e) {
                            log.error("请检查属性[{}]的范围注释是否为(true,false)！", propName);
                        }

                    }

                    //属性单位
                    List<String> unitList = new ArrayList<>();
                    if (ontology.getDataPropertyRangeAxioms(odp).size() != 0) {
                        for (OWLDataPropertyRangeAxiom op : ontology.getDataPropertyRangeAxioms(odp)) {
                            String rs = op.getRange().toString().replace(">", "");
                            String unit = rs.substring(rs.indexOf('#') + 1);
                            unitList.add(unit);
                        }
                    }
                    //添加属性单位
                    propertyType.setUnit(unitList);
                    //添加默认属性单位，默认第一个
                    if (unitList.size() != 0) {
                        propertyType.setDefaultUnit(unitList.get(0));
                    }
                    propertyTypeList.add(propertyType);

                }
            }
        }
        return propertyTypeList;

    }

    /**
     * 将一个本体类封装为EntityType
     *
     * @param cls      本体类
     * @param uuid     实体的uuid
     * @param ontology 本体模型
     * @param manager  owl本体管理类
     * @return EntityType封装类
     */
    private EntityType clsToEntityType(OWLClass cls, String uuid, OWLOntology ontology, OWLOntologyManager manager) {

        String clsName = cls.getIRI().getShortForm();
        EntityType clsEntityType = new EntityType(clsName, null);

        //设置UUID
        clsEntityType.setId(uuid);

        //获取直接子类
        List<String> subCls = getSubClass(cls, ontology);
        clsEntityType.setChildren(subCls);

        //获取所有祖先父类
        List<String> allSuperClass = getAllSuperClass(cls, ontology, manager);
        clsEntityType.setSuperTypes(allSuperClass);
        //填充属性关系(包括类本身的属性关系和祖先类的属性关系)
        List<RelationType> relations = new ArrayList<>();
        List<PropertyType> properties = new ArrayList<>();
        //自身属性
        relations.addAll(getRelationByClass(cls, ontology));
        //自身关系
        properties.addAll(getPropertiesByClass(cls, ontology));
        //如果有祖先类,填充祖先类属性关系
        if (allSuperClass != null) {
            for (String superClass : allSuperClass) {
                OWLClass owlClass = clsNameToClass(superClass, manager);
                relations.addAll(getRelationByClass(owlClass, ontology));
                properties.addAll(getPropertiesByClass(owlClass, ontology));
            }

        }

        clsEntityType.setProperties(properties);
        clsEntityType.setRelations(relations);

        //获取直接父类
        String superClsName = getSuperClass(cls, ontology);

        //如果该实体具有父类,则填充parent属性
        if (superClsName != null) {
            /*OWLClass owlClass = clsNameToClass(superClsName, manager);
            EntityType entityType = clsToEntityType(owlClass, ontology, manager);
            clsEntityType.setParent(entityType);*/
            clsEntityType.setParent(superClsName);
        } else {
            //如果该实体不具有父类,则父类为owl:thing
            /*clsEntityType.setParent(topEntityType);*/
            clsEntityType.setParent(topEntityName);
        }

        return clsEntityType;

    }


}
