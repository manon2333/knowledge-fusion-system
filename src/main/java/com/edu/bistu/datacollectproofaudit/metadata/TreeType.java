package com.edu.bistu.datacollectproofaudit.metadata;


//import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 实体类型的树结构
 */
public class TreeType {

//    private static final Logger log = (Logger) LoggerFactory.getLogger(TreeType.class);

    public static final String ROOT_ENTITY_NAME = "ROOT";

    public TreeType(String root) {
        this.root = new EntityType(root, "owl_thing");
        this.nodeMap = new HashMap<>();
    }

    private EntityType root;//树的根节点

    private Map<String, EntityType> nodeMap; //属性关系节点

    public Map<String, EntityType> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(Map<String, EntityType> nodeMap) {
        this.nodeMap = nodeMap;
    }

    @Override
    public String toString() {
        return "TreeType{" +
                "root=" + root +
                ", nodeMap=" + nodeMap +
                '}';
    }


    /**
     * 获取所有的叶子节点列表（EntityType）
     */
    public List<EntityType> getLeafNodes() {
        List<EntityType> result = new ArrayList<>();
        for (String key : this.nodeMap.keySet()) {
            EntityType entityType = nodeMap.get(key);
            if (!entityType.isLeaf()) {
                continue;
            }
            result.add(entityType);
        }
        return result;
    }

    /**
     * 获取所有的属性列表
     */
    public Map<String, PropertyType> getAllProperties() {
        Map<String, PropertyType> result = new HashMap<>();
        for (String key : this.nodeMap.keySet()) {
            if (!this.nodeMap.get(key).isLeaf()) {
                continue;
            }
            List<PropertyType> propertyTypes = this.nodeMap.get(key).getProperties();
            for (PropertyType propertyType : propertyTypes) {
                if (result.containsKey(propertyType.getName())) {
                    continue;
                }
                result.put(propertyType.getName(), propertyType);
            }
        }
        return result;
    }

    /**
     * 获取所有的关系列表
     */
    public Map<String, RelationType> getAllRelations() {
        Map<String, RelationType> result = new HashMap<>();
        for (String key : this.nodeMap.keySet()) {
            if (!this.nodeMap.get(key).isLeaf()) {
                continue;
            }
            List<RelationType> relationTypes = this.nodeMap.get(key).getRelations();
            for (RelationType relationType : relationTypes) {
                if (result.containsKey(relationType.getName())) {
                    continue;
                }
                result.put(relationType.getName(), relationType);
            }
        }
        return result;
    }


    /**
     * 获取所有的叶子节点<Id, 叶子节点名称>
     */
    public Map<String, String> getLeafName() {
        Map<String, String> result = new HashMap<>();
        for (String key : this.nodeMap.keySet()) {
            EntityType entityType = nodeMap.get(key);
            if (entityType.isLeaf()) {
                result.put(entityType.getId(), entityType.getName());
            }
        }
        return result;
    }


    /**
     * 根据属性名称获取属性单位列表
     */
    public List<String> getPropertyDefine(String propertyName) {
        for (String key : this.nodeMap.keySet()) {
            if (!this.nodeMap.get(key).isLeaf()) {
                continue;
            }
            List<PropertyType> propertyTypes = this.nodeMap.get(key).getProperties();
            for (PropertyType propertyType : propertyTypes) {
                if (propertyType.getName().equals(propertyName)) {
                    return propertyType.getUnit();
                }
            }
        }
        return null;
    }

    /**
     * 根据实体id获取实体属性列表
     */
    public List<PropertyType> getProperties(String entityId) {
        if (this.nodeMap.containsKey(entityId)) {
            return this.nodeMap.get(entityId).getProperties();
        }
        return Collections.emptyList();
    }

    /**
     * 根据实体id获取实体所有关系列表
     */
    public List<RelationType> getRelations(String entityId) {
        if (this.nodeMap.containsKey(entityId)) {
            return this.nodeMap.get(entityId).getRelations().stream().distinct().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    /**
     * 根据实体类名称获取当前类下所有直系叶子子类节点
     */
    public List<String> getAllChildren(String entityType) {
        List<String> result = new ArrayList<>();
        EntityType entity = nodeMap.get(entityType);
        List<String> children = entity.getChildren();
        if (children == null || children.size() == 0) {
            return result;
        }
        result.addAll(children);
        return result;
    }


}
