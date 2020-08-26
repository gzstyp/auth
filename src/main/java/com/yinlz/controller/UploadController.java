package com.yinlz.controller;

import com.yinlz.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 头像实名认证中心
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2019-09-30 14:06
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 http://www.yinlz.com
*/
@RestController
public class UploadController{

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private AuthService authService;

    /**手机客户端已压缩后才上传的*/
    @PostMapping("/photo")
    @ResponseBody
    public final void photo(final HttpServletRequest request,final HttpServletResponse response){
        try {
            ToolClient.responseJson(authService.authIdentity(request),response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            ToolClient.responseJson(ToolClient.exceptionJson(),response);
        }
    }
}