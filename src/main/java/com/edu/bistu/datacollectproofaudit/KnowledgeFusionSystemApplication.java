package com.edu.bistu.datacollectproofaudit;

import com.edu.bistu.datacollectproofaudit.metadata.MetadataService;
import com.edu.bistu.datacollectproofaudit.service.ProofreadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KnowledgeFusionSystemApplication implements CommandLineRunner {

    private final MetadataService metadataService;
    private ProofreadingService proofreadingService;
    private static final Logger log = LoggerFactory.getLogger(KnowledgeFusionSystemApplication.class);

    public KnowledgeFusionSystemApplication(
            @Autowired MetadataService metadataService,
                                            @Autowired ProofreadingService proofreadingService) {
        this.metadataService = metadataService;
        this.proofreadingService = proofreadingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeFusionSystemApplication.class, args);


    }


    @Override
    public void run(String... args) throws Exception {
        log.info("运行调试任务");
        debug();
    }

    private void debug() {

        //获取所有叶子节点
//        List<Map<String, String>> allLeafNodeName = metadataService.getAllLeafNodeName();
//        System.out.println("==========获取所有叶子节点==========");
//        System.out.println(allLeafNodeName);
//
//        //根据属性名称获取属性定义
//        List<String> result = metadataService.getPropertyDefine("正常速度");
//        System.out.println("==========根据属性名称获取属性定义==========");
//        System.out.println(result);
//
//        //根据实体id获取实体所有属性
//        List<EntityType> allLeafEntityTypes = metadataService.getAllLeafEntityTypes();
//        EntityType entityType = allLeafEntityTypes.get(45);
//        System.out.println("实体名称：" + entityType.getName());
//        System.out.println("==========根据实体id获取实体所有属性==========");
//
//
//        Map<String, String> nameToId = metadataService.getEntityNameToId();
//        List<PropertyType> properties = metadataService.getProperties(nameToId.get("常规动力航空母舰"));
//        for (PropertyType property : properties) {
//            System.out.println(property);
//        }
//
//
//        //根据实体id获取所有domain关系
//        List<RelationType> domainRelations = metadataService.getDomainRelations(entityType.getId());
//        System.out.println("==========根据实体id获取所有domain关系==========");
//        for (RelationType domainRelation : domainRelations) {
//            System.out.println(domainRelation);
//        }
//
//        //根据实体id获取所有range关系
//        List<RelationType> rangeRelations = metadataService.getRangeRelations(entityType.getId());
//        System.out.println("==========根据实体id获取所有domain关系==========");
//        for (RelationType rangeRelation : rangeRelations) {
//            System.out.println(rangeRelation);
//        }
//
//        //获取兄弟节点
//        List<Map<String, String>> brothers = metadataService.getBrothers(entityType.getId());
//        System.out.println("==========根据实体id获取兄弟节点==========");
//        System.out.println(brothers);
//
//        System.out.println("==========根据实体id获取后代节点==========");
//        List<Map<String, String>> allSubclass = metadataService.getAllSubclass(nameToId.get("火控雷达"));
//        System.out.println(allSubclass);


    }
}