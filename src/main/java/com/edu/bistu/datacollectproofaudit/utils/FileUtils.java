package com.edu.bistu.datacollectproofaudit.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 获取指定路径下所有的文件名
     * @param path 指定的路径
     * @return 该路径下所有的文件名，不包含路径。若指定路径不存在或出现异常，返回null
     */
    public static List<String> getFiles(String path){
        File dir = new File(path);
        if(!dir.exists()||!dir.isDirectory()){
            log.error("指定的路径:"+path+"。不存在或不是一个目录");
            return null;
        }else{
            File[] files = dir.listFiles();
            if(files==null){
                return null;
            }else{
                List<String> result = new ArrayList<>();
                for(File f: files){
                    result.add(f.getName());
                }
                return result;
            }
        }
    }

    public static List<String> readLines(File file){
        return readLines(file, StandardCharsets.UTF_8);
    }

    public static List<String> readLines(File file, Charset charset){
        if(file==null||!file.exists()||!file.isFile()){
            log.error("参数错误，文件对象为空或不是一个文件或者不存在");
            return null;
        }
        try {
            return Files.readAllLines(file.toPath(), charset);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("读取文件"+file.getName()+"的过程中出现IO异常:"+e.getMessage()+"使用的字符集："+charset.name());
            return null;
        }
    }

    public static boolean isFileExists(String dir, String file){
        File f = new File(dir, file);
        return f.isFile()&&f.exists();
    }

    public static boolean isDirExists(String parent, String... names){
        File f = Paths.get(parent, names).toFile();
        return f.isDirectory()&&f.exists();
    }

    /**
     * 使用UTF-8编码，从指定文件对象读取字符串内容，并将结果作为一个字符串返回
     * @param file 待读取的文件对象
     * @return 文件对象包含的内容
     */
	public static String readFile(File file){
        //没有指定字符集的情况下，默认按照UTF-8字符集读取文件
        return readFile(file, StandardCharsets.UTF_8);
    }

    public static String readFile(File file, Charset charset){
	    if(file==null||!file.exists()||!file.isFile()){
	        log.error("参数错误，文件对象为空或不是一个文件或者不存在");
	        return null;
        }
        try {
            return String.join( "\n",Files.readAllLines(file.toPath(), charset));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("读取文件的过程中出现IO异常:"+e.getMessage());
            return null;
        }
    }

    public static boolean writeJsonFile(String dir, String file, String json){
        if(StringUtil.containsEmptyStr(dir, file, json)){
            return false;
        }
        Path path = Paths.get(dir, file);
        return writeJsonFile(path, json, StandardCharsets.UTF_8);
    }

    public static boolean writeJsonFile(Path path, String json, Charset charset){
        if(path==null){
            return false;
        }
	    if(StringUtil.isEmpty(json)){
            return false;
        }
        try {
            Files.write(path, Collections.singletonList(json), charset);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    /**
     * 删除指定路径下的所有子目录和文件，但不删除该路径对应的目录本身
     * @param path 指定的路径
     * @return 删除操作是否成功
     */
    public static boolean deleteSubDirs(String path){
        File parent = new File(path);
        if(parent.exists()&&parent.isDirectory()){
            //path存在且是一个目录
            File[] files = parent.listFiles();
            if(files==null){
                //files为空只有可能是： 1）parent不是一个目录；2）IO异常
                //parent必然是一个目录，所以如果files==null，说明出现IO异常
                return false;
            }
            for(File sub: files){
                if(!delete(sub)){
                    return false;
                }
            }
            return true;
        }else{
            //path不存在或者不是一个目录
            return false;
        }
    }

    /**
     * 递归删除file代表的文件或目录
     * @param file 要删除的文件或目录
     * @return 删除操作是否成功
     */
    private static boolean delete(File file){
        if(file==null||!file.exists()){
            return false;
        }
        if(!file.isFile()){
            //删除目录
            File[] subs = file.listFiles();
            if(subs==null){
                return false;
            }
            for(File sub: subs){
                if(!delete(sub)){
                    return false;
                }
            }
            //子文件/目录全部删除了
        }
        //删除文件
        return file.delete();
    }

    /**
     * 将字符串转出到指定文件
     * @param saveFile
     * @param content
     */
    public static void toFile(File saveFile, String content) {
        File parent = saveFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(saveFile));
            out.print(content);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
