package com.hxw.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.hxw.ucenter.mapper.XcMenuMapper;
import com.hxw.ucenter.mapper.XcUserMapper;
import com.hxw.ucenter.model.dto.AuthParamsDto;
import com.hxw.ucenter.model.dto.XcUserExt;
import com.hxw.ucenter.model.po.XcMenu;
import com.hxw.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {


    @Autowired
    ApplicationContext applicationContext;
    @Resource
    private XcMenuMapper xcMenuMapper;
    @Resource
    private XcUserMapper xcUserMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求格式不符合标准");
            throw new RuntimeException("认证请求数据格式不对");
        }
        String username = authParamsDto.getUsername();
        String authType = authParamsDto.getAuthType();
        AuthService bean = applicationContext.getBean(authType + "_authService", AuthService.class);
        //开始认证,认证成功拿到用户信息
        XcUserExt xcUserExt = bean.execute(authParamsDto);
        return getUserPrincipal(xcUserExt);
    }


    //根据XcUserExt构造一个UserDetails对象
    public UserDetails getUserPrincipal(XcUserExt user) {
        //调用mapper查询数据库用户的权限
        List<XcMenu> list = xcMenuMapper.selectPermissionByUserId(user.getId());
        String[] authorizes = {};
        List<String> authorizeList = new ArrayList<>();
        list.forEach(menu -> {
            authorizeList.add(menu.getCode());
        });
        if (authorizeList.size() > 0) {
            authorizes = authorizeList.toArray(new String[0]);
        }
        //将用户信息转化为json字符串
        user.setPassword(null);
        String jsonString = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(jsonString).password("").authorities(authorizes).build();
        return userDetails;
    }


}
