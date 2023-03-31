package com.edu.bistu.datacollectproofaudit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 系统配置数据
 *
 * @author chenruoyu
 */
@ConfigurationProperties(prefix = "spring.datasource")
@Component
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class DbConfig {
    private String url;

    private String userName;

    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
