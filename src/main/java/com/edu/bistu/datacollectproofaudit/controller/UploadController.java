package com.edu.bistu.datacollectproofaudit.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.edu.bistu.datacollectproofaudit.annotation.UserLoginToken;
import com.edu.bistu.datacollectproofaudit.config.SysConfig;
import com.edu.bistu.datacollectproofaudit.service.DatasourceService;
import com.edu.bistu.datacollectproofaudit.utils.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class UploadController {

    private SysConfig sysConfig;

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    public UploadController(@Autowired SysConfig sysConfig){
        this.sysConfig=sysConfig;

    }

    /**
     * 上传图片
     * @param file 文件
     * @param request
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value="/upload",method= RequestMethod.POST)
    @ResponseBody
    public QueryResponse uploadFile(MultipartFile file, HttpServletRequest request) {
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        String fileName;
        File fileDir;
        try{
            //创建文件存放路径
            String dir=sysConfig.getUpload();
            fileDir = new File(dir);
            if(!fileDir.exists())
                fileDir.mkdirs();
            //生成文件在服务器端存放的名字
            String fileSuffix=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            fileName= UUID.randomUUID().toString()+fileSuffix;
            File files = new File(fileDir+"/"+fileName);
            //上传
            file.transferTo(files);
        }catch(Exception e) {
            e.printStackTrace();
            log.info("图片上传失败！");
            return QueryResponse.genErr("图片上传失败!");
        }
        jsonObject.put("pictureName","/custom/"+fileName);
        log.info("图片上传成功！");
        response.setSuccess(true);
        response.setMsg("图片上传成功！");
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        return response;
    }


    /**
     * 多张图片上传
     * @param files 多个文件
     * @param request
     * @return QueryResponse
     */
    @UserLoginToken
    @RequestMapping(value="/uploadMultipartFile",method= RequestMethod.POST)
    @ResponseBody
    public QueryResponse multipleImageupload(MultipartFile[] files, HttpServletRequest request) {
        System.out.println("上传的图片数："+files.length);
        QueryResponse response = new QueryResponse();
        JSONObject jsonObject = new JSONObject();
        List<String> pictureNames=new ArrayList<>();
        String fileName;
        File fileDir;
        for(MultipartFile file:files){
            try{
                //创建文件存放路径
                String dir=sysConfig.getUpload();
                fileDir = new File(dir);
                if(!fileDir.exists())
                    fileDir.mkdirs();
                //生成文件在服务器端存放的名字
                String fileSuffix=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                fileName= UUID.randomUUID().toString()+fileSuffix;
                File newfile = new File(fileDir+"/"+fileName);
                //上传
                file.transferTo(newfile);
                pictureNames.add("/custom/"+fileName);
            }catch(Exception e) {
                e.printStackTrace();
                log.info("图片上传失败！");
                return QueryResponse.genErr("图片上传失败!");
            }
        }
        jsonObject.put("pictureNames",pictureNames);
        log.info("图片上传成功！");
        response.setSuccess(true);
        response.setMsg("图片上传成功！");
        response.setJson(JSON.toJSONString(jsonObject, SerializerFeature.DisableCircularReferenceDetect));
        return response;
    }
}
