package com.yinlz.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yinlz.controller.ToolClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 身份认证工具
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2019-10-31 14:20
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 http://www.yinlz.com
*/
public final class ToolAuth{

    private final static String loginName = "yunzhengbao"; //账号

    private final static String password = "yunzhengbao1202"; //密码

    private final static String srcUrl = "https://www.miniscores.net:8313/CreditFunc/v2.1/IdNamePhotoCheck"; //南京甄视身份认证服务接口地址
    private final static String urlCompareService = "https://www.miniscores.net:8315/CreditFunc/picCompareService"; //南京甄视两张照片比对

    private final static String url = "http://api.chinadatapay.com/communication/personal/2061"; //数据宝身份认证api

    private final static String img_upload = "https://file.chinadatapay.com/img/upload"; //数据宝身份认证上传图片地址

    private final static String getBase64(final File file) throws Exception{
        final InputStream in = new FileInputStream(file);
        final byte[] data = new byte[in.available()];
        in.read(data);
        in.close();
        return new BASE64Encoder().encode(data);
    }

    public final static String authImage(final String realName,final String idCard,final String filePath) throws Exception{
        final File file = new File(filePath);
        final String image = getBase64(file);
        final HashMap<String,String> headers = new HashMap<>();
        headers.put("mvTrackId",ToolClient.getIdsChar32());
        final JSONObject totalJsonObj = new JSONObject();
        totalJsonObj.put("loginName", loginName);
        totalJsonObj.put("pwd", password);
        totalJsonObj.put("serviceName", "FaceCompare");
        final JSONObject paramJsonObj = new JSONObject();
        paramJsonObj.put("image1",image);
        paramJsonObj.put("image2",image);
        totalJsonObj.put("param", paramJsonObj);
        return requestPostHeader(srcUrl,totalJsonObj,headers);
    }

    public final static String authIdentity(final String realName,final String idCard,final String filePath) throws Exception{
        final File file = new File(filePath);
        final String image = getBase64(file);
        final HashMap<String,String> headers = new HashMap<>();
        headers.put("mvTrackId",ToolClient.getIdsChar32());
        final JSONObject totalJsonObj = new JSONObject();
        totalJsonObj.put("loginName", loginName);
        totalJsonObj.put("pwd", password);
        totalJsonObj.put("serviceName", "IdNamePhotoCheck");
        final JSONObject paramJsonObj = new JSONObject();
        paramJsonObj.put("idCard",idCard);
        paramJsonObj.put("name",realName);
        paramJsonObj.put("image", image);
        totalJsonObj.put("param", paramJsonObj);
        return requestPostHeader(srcUrl,totalJsonObj,headers);
    }

    /**同步请求-带请求头且封装多实体对象请求,即params里的value可以是基本类型(String,Integer)、实体、JSONObject对象*/
    protected final static String requestPostHeader(final String url,final JSONObject params,final HashMap<String,String> headers) throws Exception {
        final OkHttpClient client = new OkHttpClient();
        final Request.Builder requestbuilder = new Request.Builder().url(url);
        for(final String key : headers.keySet()){
            final String value = headers.get(key);
            if(value != null && value.length() > 0){
                if(value.length() == 1 && value.equals("_"))
                    continue;
                requestbuilder.addHeader(key,value);
            }
        }
        final Request request = requestPostBeans(url,params);
        final Response response = client.newCall(request).execute();
        return response.body().string();
    }

    protected final static Request requestPostBeans(final String url,final JSONObject params){
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),JSON.toJSONString(params));
        return new Request.Builder().post(requestBody).tag(url+params).url(url).build();
    }
}