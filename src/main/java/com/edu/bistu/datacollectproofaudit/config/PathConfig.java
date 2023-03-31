package com.edu.bistu.datacollectproofaudit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
class PathConfig implements WebMvcConfigurer {
    private final SysConfig sysConfig;

    public PathConfig(@Autowired SysConfig sysConfig) {
        this.sysConfig = sysConfig;
    }

    /**
     * 图片路径配置
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imgs_path = sysConfig.getUpload();
        registry.addResourceHandler("/custom/**").addResourceLocations("file:" + imgs_path + "/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

}
