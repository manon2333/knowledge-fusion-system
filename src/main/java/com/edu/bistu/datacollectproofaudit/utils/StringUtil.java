package com.edu.bistu.datacollectproofaudit.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);


    /**
     * 判断字符串是否为空（包括null或trim后为""的字符串）
     * @param str 待判断的字符串
     * @return 若str为null或者trim后为""，返回true，否则返回false
     */
    public static boolean isEmpty(String str){
        return str==null||"".equalsIgnoreCase(str.trim());
    }

    /**
     * 判断字符串是否为空（包括null或trim后为""的字符串或"\N"）
     * @param str 待判断的字符串
     * @return 若str为null或者trim后为""、"\N"或者"null"，返回true，否则返回false
     */
    public static boolean isEmptyN(String str){
        return isEmpty(str)||"\\N".equalsIgnoreCase(str)||"null".equalsIgnoreCase(str);
    }

    /**
     * 判断可变长字符串参数中是否包含空字符串
     * @param strs 待判断的参数列表
     * @return 若strs中的任何一个参数为空，则返回true；否则返回false
     */
    public static boolean containsEmptyStr(String... strs){
        for(String str: strs){
            if(isEmpty(str)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断可变长数组中包含的字符串参数是否都为空
     * @param strs 待判断的参数列表
     * @return 若strs中的所有参数都为空，则返回true；否则返回false
     */
    public static boolean allEmpty(String... strs){
        for(String str: strs){
            if(!isEmpty(str)){
                return false;
            }
        }
        return true;
    }

    public static List<String> split(String str, String... delimiters){
        List<String> result = new ArrayList<>();
        if(isEmpty(str)){
            return result;
        }
        for(String delimiter: delimiters){
            //逐个尝试分隔符
            if(str.contains(delimiter)){
                String[] srs = str.split(delimiter);
                for(String s:srs){
                    if(!isEmpty(s))
                        result.add(s.trim());
                }
                return result;
            }
        }
        //所有的分隔符在字符串中都不包含
        result.add(str);
        return result;
    }

    /**
     * 判断两个字符串是否相等：
     * 1）若都为null或都为空字符串（""）认定为相等。
     * 2）若一个为null，另一个不是（也不是空字符串），认定为不等。
     * 3）若两个字符串都不为空，且相等，则认定为相等，否则不相等
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相等的判断结果
     */
    public static boolean strEquals(String str1, String str2){
        if(isEmpty(str1)&&isEmpty(str2)){
            //都为null，或都为""，或一个为null一个为""
            return true;
        }else{
            if(isEmpty(str1)||isEmpty(str2)){
                //只有一个为null或"", 另一个不为空
                return false;
            }else{
                //都不为空
                return str1.equalsIgnoreCase(str2);
            }
        }
    }

    /**
     * 1）当str1和str2中有一个字符串为空（null或""），另一个不为空时，将不为空的字符串返回；
     * 2）若str1和str2都为空字符串时，返回""；
     * 3）若str1和str2不相等，返回null；
     * 4）若str1和str2不为空字符串，且相等，返回任何一个；
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 1）当str1和str2中有一个字符串为空（null或""），另一个不为空时，将不为空的字符串返回；
     * 2）若str1和str2都为空字符串时，返回""；
     * 3）若str1和str2不相等，返回null；
     * 4）若str1和str2不为空字符串，且相等，返回任何一个；
     */
    public static String getNotNullOrEqual(String str1, String str2){
        if(!isEmpty(str1)&&!isEmpty(str2)){
            //都不为空
            if(str1.equalsIgnoreCase(str2)){
                //str1和str2相等
                return str1;
            }else{
                //str1和str2严格不相等
                return null;
            }
        }else{
            //至少有一个为空
            if(!isEmpty(str1)){
                return str1;
            }else if(!isEmpty(str2)){
                return str2;
            }else{
                //两个都是空字符串，返回""；
                // 这是为了避免两个字符串都为null的情况下，如果返回null，
                // 和两个字符串严格不相等时返回null无法区分
                return "";
            }
        }
    }

    /**
     * 1）当str1和str2中有一个字符串为空（null或""），另一个不为空时，将不为空的字符串返回；
     * 2）若str1和str2都为空字符串时，返回null；
     * 3）若str1和str2都不为空，返回str1。
     * @param str1 字符串1
     * @param str2 字符串2
     * @return
     * 1）当str1和str2中有一个字符串为空（null或""），另一个不为空时，将不为空的字符串返回；
     * 2）若str1和str2都为空字符串时，返回null；
     * 3）若str1和str2都不为空，返回str1。
     */
    public static String getNotNull(String str1, String str2){
        if(!isEmpty(str1)&&!isEmpty(str2)){
            //都不为空
            return str1;
        }else{
            //至少有一个为空
            if(!isEmpty(str1)){
                return str1;
            }else if(!isEmpty(str2)){
                return str2;
            }else{
                return null;
            }
        }
    }

    /**
     * 与getNotNullOrEqual类似但不相同。
     * 若两个字符串都不为空，将str1和str2连接起来，用delimiter作为两个字符之间的分隔符；
     * 若两个字符串都为空，返回null；
     * 若两个字符串中的一个为空，返回不为空的字符串。
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 若两个字符串都不为空，将str1和str2连接起来，用delimiter作为两个字符之间的分隔符；
     * 若两个字符串都为空，返回null；
     * 若两个字符串有且仅有一个为空，返回不为空的那个字符串。
     */
    public static String concat(String str1, String str2, String delimiter){
        if(!isEmpty(str1)&&!isEmpty(str2)){
            //都不为空（null或""）
            return str1+delimiter+str2;
        }else{
            //至少有一个为空（null或""）
            if(!isEmpty(str1)){
                return str1;
            }else{
                return str2;
            }
        }
    }

    /**
     * 将时间字符串转换为时间戳，时间字符串的格式由pattern参数给出，
     * 如：时间字符串可能为 "2001-05-17 00:00:00",
     *    对应的pattern为 "uuuu-MM-dd hh:mm:ss",
     *    转换后变成长整数，表示的是从Unix epoch到该时间的毫秒数
     * @param time    表示时间的字符串
     * @param patterns 时间格式
     * @return 转换后的时间戳
     */
    public static long time2ts(String time, List<String> patterns){
        for(String pattern: patterns){
            try{
                DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime formatted = LocalDateTime.parse(time, format);
                return formatted.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            }catch(DateTimeParseException e){
                log.debug("使用时间格式[{}]无法解析日期字符串[{}]", pattern, time);
            }
        }
        return -1;
    }

    /**
     * 将表示日期的字符串转换为时间戳，日期字符串的格式由pattern参数给出，
     * 如：日期字符串可能为 "2001-05-17",
     *    对应的pattern为 "uuuu-MM-dd",
     *    转换后变成长整数，表示的是从Unix epoch到该日 00:00:00的毫秒数
     * @param date    表示日期的字符串
     * @param pattern 时间格式
     * @return 转换后的时间戳
     */
    public static long date2ts(String date, String pattern){
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDate formatted = LocalDate.parse(date, format);
        return formatted.atTime(0,0,0).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /**
     * 将给定的字符串str截断到给定的长度
     * @param str 给定的字符串
     * @param length 截取长度
     * @return 截取后的字符串
     */
    public static String truncateString(String str, int length){
        if (isEmpty(str)){
            return str;
        }else{
            if(length>str.length()){
                return str;
            }else{
                return str.substring(0, length);
            }
        }
    }

    /**
     * 将时间戳转换为指定格式的时间字符串
     * @param timestamp 时间戳，以毫秒计
     * @param pattern   时间格式，如"yyyyMMdd"
     * @return 将时间戳按照指定格式格式化后的字符串
     */
    public static String ts2time(long timestamp, String pattern){
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return localDateTime.format(format);
    }

    /**
     * 将时间戳转换为指定格式的日期字符串
     * @param timestamp 时间戳，以毫秒计
     * @param pattern   时间格式，如"yyyyMMdd"
     * @return 将时间戳按照指定格式格式化后的字符串
     */
    public static String ts2date(long timestamp, String pattern){
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDate();
        return localDate.format(format);
    }

    /**
     * 判断字符串是否含有中文
     * @param str 需要判断的字符串
     * @return true含有中文 false 未含中文
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 提取字符串中的所有数值信息，以集合（Integer）的形式返回
     * @param str 需要提取的字符串
     * @return  数值信息的集合
     */
    public static List<Integer> getNumOfText(String str){
        str=str.trim(); //去空格
        List<Integer> nums= new ArrayList<>();
        if(str != null && !"".equals(str)) {
            String num =""; //存储当前数值
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    num += str.charAt(i);   //相邻数字拼接
                }else if(!num.equals("")){  //当前数值存储完毕
                    nums.add(Integer.valueOf(num)); //将数值存入
                    num="";
                }
            }
        }
        return nums;
    }

    /**
     * 判断在str中,在str1与str2第一次出现的位置，是否存在str2邻接与str1之后
     * @param str1 邻接字符串
     * @param str2 被邻接字符串
     * @param str 用于判断邻接关系的总字符串
     * @return str2邻接与str1时返回true，其他情况返回false
     */
    public static boolean isAdjacent(String str1,String str2,String str){
        if((str.indexOf(str1)+str1.length()==str.indexOf(str2))){
            return true;
        }
        return false;
    }

    // 往字符串数组追加新数据
    public static String[] arrayInsert(String[] arr, String... str) {
        int size = arr.length; // 获取原数组长度
        int newSize = size + str.length; // 原数组长度加上追加的数据的总长度

        // 新建临时字符串数组
        String[] tmp = new String[newSize];
        // 先遍历将原来的字符串数组数据添加到临时字符串数组
        for (int i = 0; i < size; i++) {
            tmp[i] = arr[i];
        }
        // 在末尾添加上需要追加的数据
        for (int i = size; i < newSize; i++) {
            tmp[i] = str[i - size];
        }
        return tmp; // 返回拼接完成的字符串数组
    }

    //计算两个字符串之间的编辑距离
    public static int editDistance(String word1, String word2) {

        int dp[][] = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i < word1.length() + 1; i++) {
            // 从i个字符变成0个字符，需要i步（删除）
            dp[i][0] = i;
        }
        for (int i = 0; i < word2.length() + 1; i++) {
            // 当从0个字符变成i个字符，需要i步(增加)
            dp[0][i] = i;
        }

        for (int i = 1; i < word1.length() + 1; i++) {
            for (int j = 1; j < word2.length() + 1; j++) {
                //当相同的时，dp[i][j] = dp[i - 1][j - 1]
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    //当不同的时候，我们需要求三种操作的最小值
                    //其中dp[i - 1][j - 1]表示的是替换，dp[i - 1][j]表示删除字符，do[i][j - 1]表示的是增加字符
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }

    public static String[] arrayDelete(String array[],int index) {
        //数组的删除其实就是覆盖前一位
        String[] arrNew = new String[array.length - 1];
        for (int i = 0; i < array.length - 1; i++) {
            if (i < index) {
                arrNew[i] = array[i];
            } else {
                arrNew[i] = array[i + 1];
            }
        }
        return arrNew;
    }

    public static int getIndex(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) {
                return i;                  //字符串时，换为 equals
            }
        }
        return -1;//如果未找到返回-1
    }

    /**
     * 将null改为“，避免json传给前段时丢值
     * @param a
     * @return
     */
    public static String changeFormat(String a){
        if(a==null){
            a="";
        }
            return a;
    }
}