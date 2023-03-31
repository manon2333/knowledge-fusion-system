package com.edu.bistu.datacollectproofaudit.metadata;


import java.util.ArrayList;
import java.util.List;

/**
 * 实体类型元数据类
 * @author chenruoyu
 */
public class EntityType {

    public static final EntityType owl_thing = new EntityType("root", null);


    public EntityType() {
    }

    public EntityType(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * 实体的唯一id标识
     */
    private String id;

    /**
     * 显示名称的别名，不用于显示，但可以用于后期训练实体属性标注模型
     */
    private List<String> alias;

    /**
     * 名称（实体类型的唯一标识），用来存储到Neo4j知识库里
     */
    private String name;

    /**
     * 实体的级别
     */
    private int level;

    /**
     * 当前类型的实体应该包含的属性列表
     */
    private List<PropertyType> properties;

    /**
     * 当前类型的实体应该包含的关系列表
     */
    private List<RelationType> relations;

    /**
     * 当前类别的父亲及祖先类
     */
    private List<String> superTypes;

    /**
     * 当前类的直接父类
     */
//    private EntityType parent;
    private String parent;

    /**
     * 当前类的直接子类
     */
    private List<String> children;

    /**
     * 向EntityType中添加孩子节点
     *
     * @param child 孩子节点的名称
     */
    protected void addChildren(String child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        if (!this.children.contains(child)) {
            this.children.add(child);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<PropertyType> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyType> properties) {
        this.properties = properties;
    }

    public List<RelationType> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationType> relations) {
        this.relations = relations;
    }

    public List<String> getSuperTypes() {
        return superTypes;
    }

    public void setSuperTypes(List<String> superTypes) {
        this.superTypes = superTypes;
    }

//    public EntityType getParent() {
//        return parent;
//    }
//
//    public void setParent(EntityType parent) {
//        this.parent = parent;
//    }


    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    //判断该实体节点是否为叶子节点
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    @Override
    public String toString() {
        return "EntityType{" +
                "id='" + id + '\'' +
                ", alias=" + alias +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", properties=" + properties +
                ", relations=" + relations +
                ", superTypes=" + superTypes +
                ", parent=" + parent +
                ", children=" + children +
                '}';
    }
}
