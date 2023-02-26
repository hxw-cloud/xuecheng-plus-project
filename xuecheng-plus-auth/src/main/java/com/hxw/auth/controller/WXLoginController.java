package com.hxw.auth.controller;

import com.hxw.ucenter.model.po.XcUser;
import com.hxw.ucenter.service.Impl.WxAuthServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
public class WXLoginController {
    @Resource
    private WxAuthServiceImpl wxAuthService;

    @RequestMapping("/vxLogin")
    public String wxLogin(String code, String state) throws IOException {
        //拿授权码去申请令牌，查询用户
        XcUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null) {
            return "redirect:http://www.xuecheng-plus.com/error.html";
        } else {
            //重定向到登陆页面自动登录
            String username = xcUser.getUsername();
            return "redirect:http://www.xuecheng-plus.com/sign.html?username=" + username + "&authType=wx";
        }
    }
}
