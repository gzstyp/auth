package com.yinlz.service;

import com.yinlz.controller.ConfigFile;
import com.yinlz.controller.ToolClient;
import com.yinlz.dao.DaoBase;
import com.yinlz.tool.ToolAuth;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 实名认证中心
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2019-10-31 20:02
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 http://www.yinlz.com
*/
@Service
public class AuthService{

    private final String select_file = ToolClient.createJson(ConfigFile.code199,"请选择上传文件");
    private final String oversize = ToolClient.createJson(ConfigFile.code199,"操作失败,某个文件过大");
    private final String type_photo = "photo/";
    //上传图片的限制大小
    @Value("${limit_size_image}")
    private Long limit_size_image;
    //上传文件的根目录
    @Value("${dir_linux}")
    private String dir_folder;
    //头像访问服务器
    @Value("${domain_avatar}")
    private String domain_avatar;

    @Resource
    private DaoBase daoBase;

    /**
     * 上传图片并实名认证
     * @作者 田应平
     * @版本 v1.0
     * @创建时间 2019/12/11 20:37
     * @QQ号码 444141300
     * @Email service@yinlz.com
     * @官网 <url>http://www.yinlz.com</url>
    */
    public String authIdentity(final HttpServletRequest request){
        final HashMap<String,String> params = ToolClient.getFormFields(request);
        final String validate = ToolClient.validateForm(params,new String[]{"realName","idCard"});
        if(validate!= null){
            return validate;
        }
        MultipartHttpServletRequest mhsr = null;
        try {
            mhsr =  (MultipartHttpServletRequest) request;
        } catch (Exception e) {
            return ToolClient.createJson(ConfigFile.code198,"请上传人脸照片");
        }
        if(mhsr == null){
            return ToolClient.createJson(ConfigFile.code198,"未上传人脸照片");
        }
        final DiskFileItemFactory fac = new DiskFileItemFactory();
        final ServletFileUpload upload = new ServletFileUpload(fac);
        String originalPath = null;
        try {
            upload.setHeaderEncoding("utf-8");
            mhsr.setCharacterEncoding("utf-8");
            final String sys = File.separator;
            String dirType = sys;
            final String savePath = mhsr.getSession().getServletContext().getRealPath(dirType);
            final File directory = new File(savePath);
            if(!directory.exists()){
                directory.mkdirs();
            }
            final Map<String,MultipartFile> files = mhsr.getFileMap();
            if(files != null && files.size() > 1){
                return ToolClient.createJson(ConfigFile.code198,"不要上传过多的照片");
            }
            MultipartFile file = null;
            for(final String key : files.keySet()){
                file = mhsr.getFile(key);
                break;
            }
            if(file == null || file.isEmpty()){
                return select_file;
            }
            if(file.getSize() > limit_size_image){
                return oversize;
            }
            final String idCard = params.get("idCard");
            final String realName = params.get("realName");
            final String existAvatar = queryByExist(idCard,realName);
            if(existAvatar != null){
                final HashMap<String,String> map = new HashMap<String,String>();
                map.put("url",existAvatar);
                map.put("msg","已重复认证");
                return ToolClient.queryJson(map);
            }
            final String name = file.getOriginalFilename();
            final String extName = name.substring(name.lastIndexOf("."));
            final String fileName = ToolClient.getIdsChar32() + extName;
            final String dir_day = new SimpleDateFormat("yyyyMMdd").format(new Date())+"/";
            final String base_dir = dir_folder + type_photo + dir_day;
            final String dirDayOriginal = base_dir + "original/";//原图路径
            final File fileDir = new File(dirDayOriginal);
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }
            originalPath = dirDayOriginal + fileName;
            file.transferTo(new File(originalPath));
            final String url = domain_avatar + dir_day + "original/" + fileName;
            final String data = ToolAuth.authIdentity(realName,idCard,originalPath);
            final HashMap<String,String> jsonObject = ToolClient.parseJsonObject(data);
            final HashMap<String,String> detail = ToolClient.parseJsonObject(jsonObject.get("detail"));
            final String result = jsonObject.get("RESULT");
            params.put("kid",ToolClient.getIdsChar32());
            params.put("filePath",originalPath);
            params.put("url",url);
            final int rows = daoBase.execute("auths.addAuth",params);
            if(result.equals("1") && rows > 0){
                final HashMap<String,String> map = new HashMap<String,String>();
                map.put("url",url);
                return ToolClient.queryJson(map);
            }else{
                final String resultMsg = detail.get("resultMsg");
                final String message = jsonObject.get("MESSAGE");
                ToolClient.delFileByThread(originalPath);//已认证时删除文件
                return ToolClient.createJson(ConfigFile.code198,(resultMsg == null || resultMsg.length() <= 0) ? message : resultMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToolClient.delFileByThread(originalPath);//已认证时删除文件
            return ToolClient.createJson(ConfigFile.code204,"认证失败");
        }
    }

    /**根据用户份证号查询之前是否认证*/
    public String queryByExist(final String idCard,final String realName) throws Exception{
        final HashMap<String,String> params = new HashMap<String,String>();
        params.put("idCard",idCard);
        params.put("realName",realName);
        return daoBase.queryForString("auths.queryByExist",params);
    }
}