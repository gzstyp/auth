package com.yinlz.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 客户端响应工具类
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2019-09-30 14:36
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 http://www.yinlz.com
*/
public final class ToolClient{

    public final static void responseJson(final Object jsonObject,final HttpServletResponse response){
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control","no-cache");
        PrintWriter writer = null;
        try{
            writer = response.getWriter();
            if(jsonObject == null){
                writer.write(createJson(ConfigFile.code201,ConfigFile.msg201));
                writer.flush();
                writer.close();
                return;
            }
            if(jsonObject instanceof String){
                writer.write(JSON.parse(jsonObject.toString()).toString());
                writer.flush();
                writer.close();
                return;
            }else{
                writer.write(JSONArray.toJSONString(jsonObject));
                writer.flush();
                writer.close();
                return;
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(null != writer){
                writer.close();
            }
        }
    }

    public static final String createJson(final Integer code,final String msg){
        final JSONObject json = new JSONObject();
        json.put(ConfigFile.code,code);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

    public final static String exceptionJson(){
        final JSONObject json = new JSONObject();
        json.put(ConfigFile.code,ConfigFile.code204);
        json.put(ConfigFile.msg,ConfigFile.msg204);
        return json.toJSONString();
    }

    public final static String exceptionJson(final String msg){
        final JSONObject json = new JSONObject();
        json.put(ConfigFile.code,ConfigFile.code204);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

    public final static String queryJson(final Object object){
        final JSONObject json = new JSONObject();
        if(isBlank(object)){
            return queryEmpty();
        }
        if(object instanceof Map<?,?>){
            final Map<?,?> map = (Map<?,?>) object;
            if(map == null || map.size() <= 0){
                queryEmpty();
            }else {
                json.put(ConfigFile.code,ConfigFile.code200);
                json.put(ConfigFile.msg,ConfigFile.msg200);
                json.put(ConfigFile.data,object);
                return json.toJSONString();
            }
        }
        if(object instanceof List<?>){
            final List<?> list = (List<?>) object;
            if(list == null || list.size() <= 0){
                return queryEmpty();
            }else {
                if (isBlank(list.get(0))){
                    return queryEmpty();
                }else {
                    json.put(ConfigFile.code,ConfigFile.code200);
                    json.put(ConfigFile.msg,ConfigFile.msg200);
                    json.put(ConfigFile.data,object);
                    final String jsonObj = json.toJSONString();
                    final JSONObject j = JSONObject.parseObject(jsonObj);
                    final String listData = j.getString(ConfigFile.data);
                    if (listData.equals("[{}]")){
                        return queryEmpty();
                    }
                    return jsonObj;
                }
            }
        }
        if(String.valueOf(object).toLowerCase().equals("null") || String.valueOf(object).replaceAll("\\s*", "").length() == 0){
            return queryEmpty();
        }else {
            json.put(ConfigFile.code,ConfigFile.code200);
            json.put(ConfigFile.msg,ConfigFile.msg200);
            json.put(ConfigFile.data,object);
            final String jsonObj = json.toJSONString();
            final JSONObject j = JSONObject.parseObject(jsonObj);
            final String obj = j.getString(ConfigFile.data);
            if (obj.equals("{}")){
                return queryEmpty();
            }
            return jsonObj;
        }
    }

    private static final String queryEmpty(){
        final JSONObject json = new JSONObject();
        json.put(ConfigFile.code,ConfigFile.code201);
        json.put(ConfigFile.msg,ConfigFile.msg201);
        return json.toJSONString();
    }

    /**
     * 请求参数有误
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/10/22 20:39
     */
    public final static String paramsError(){
        final JSONObject json = new JSONObject();
        json.put(ConfigFile.code,ConfigFile.code202);
        json.put(ConfigFile.msg,ConfigFile.msg202);
        return json.toJSONString();
    }

    public final static boolean isBlank(final Object obj){
        if(obj == null)
            return true;
        final String temp = String.valueOf(obj);
        if(temp.toLowerCase().equals("null"))
            return true;
        final String key = obj.toString().replaceAll("\\s*","");
        if(key.equals("") || key.length() <= 0)
            return true;
        if(key.length() == 1 && key.equals("_"))
            return true;
        if(obj instanceof List<?>){
            final List<?> list = (List<?>) obj;
            return list == null || list.size() <= 0;
        }
        if(obj instanceof Map<?,?>){
            final Map<?,?> map = (Map<?,?>) obj;
            return map == null || map.size() <= 0;
        }
        if(obj instanceof String[]){
            boolean flag = false;
            final String[] require = (String[]) obj;
            for(int i = 0; i < require.length; i++){
                if(require[i] == null || require[i].length() == 0 || require[i].replaceAll("\\s*","").length() == 0){
                    flag = true;
                    break;
                }
            }
            return flag;
        }
        if(obj instanceof JSONObject){
            final JSONObject json = (JSONObject) obj;
            return json.isEmpty();
        }
        return false;
    }

    /**多线程下生成32位唯一的字符串*/
    public final static String getIdsChar32(){
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextInt(),random.nextInt()).toString().replaceAll("-","");
    }

    public final static void responseException(final HttpServletResponse response){
        responseJson(exceptionJson(),response);
        return;
    }

    /**
     * 开线程访问服务器删除图片
     * @date 2016年12月9日 下午2:37:20
    */
    public final static void delFileByThread(final List<String> files) {
        try {
            new Thread(){
                public void run() {
                    //删除指定文件
                    for(int i = 0; i < files.size(); i++){
                        final String path = files.get(i);
                        final File file = new File(path);
                        if(file.isFile()){
                            file.delete();
                        }
                    }
                }
            }.start();
        } catch (Exception e){
        }
    }

    /**
     * 开线程访问服务器删除图片
     * @date 2019年10月31日 16:41:40
     */
    public final static void delFileByThread(final String filePath) {
        try {
            new Thread(){
                public void run() {
                    final File file = new File(filePath);
                    if(file.isFile()){
                        file.delete();
                    }
                }
            }.start();
        } catch (Exception e){
        }
    }

    /**
     * 验证必要的字段是否为空,一般在service层调用,如果返回为 null 则验证成功,否则失败;适用于增、删、改、查操作!
     */
    public final static String validateForm(final HashMap<String,String> formData,final String[] fields){
        if(isBlank(formData) || isBlank(fields)){
            return paramsError();
        }
        if(!isBlank(fields)){
            boolean flag = false;
            for(String p : fields){
                if(isBlank(formData.get(p))){
                    flag = true;
                    break;
                }
            }
            if(flag){
                return paramsError();
            }
        }
        return null;
    }

    /**
     * 获取表单的请求参数,不含文件域,返回的是HashMap<String,String>
     * @param request
     */
    public final static HashMap<String,String> getFormFields(final HttpServletRequest request){
        final HashMap<String,String> params = new HashMap<String,String>();
        final Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()){
            final String key = paramNames.nextElement();
            final String value = request.getParameter(key);
            if(value != null && value.length() > 0){
                if(value.length() == 1 && value.equals("_"))
                    continue;
                params.put(key,value.trim());
            }
        }
        return params;
    }

    public final static HashMap<String,String> parseJsonObject(final String json){
        final HashMap<String, String> jsonMap = new HashMap<String, String>();
        if(json == null || json.length() <= 0) return jsonMap;
        try {
            final JSONObject jsonObject = JSONObject.parseObject(json);
            for (final String key : jsonObject.keySet()){
                final Object obj = jsonObject.get(key);
                final String value = (String)jsonObject.get(key);
                if(value != null && value.length() > 0){
                    if(value.length() == 1 && value.equals("_"))
                        continue;
                    jsonMap.put(key,value.trim());
                }
            }
            return jsonMap;
        } catch (Exception e){
            return jsonMap;
        }
    }
}