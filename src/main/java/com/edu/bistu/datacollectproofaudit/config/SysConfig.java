package com.edu.bistu.datacollectproofaudit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 系统配置数据
 *
 * @author chenruoyu
 */
@ConfigurationProperties(prefix = "kg.proofread")
@Component
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class SysConfig {

    private String metaservice_ip;

    private String upload;

    private String metadata;

    public String getMetaservice_ip() {

        return metaservice_ip;
    }

    public void setMetaservice_ip(String metaservice_ip) {

        this.metaservice_ip = metaservice_ip;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    //

}
