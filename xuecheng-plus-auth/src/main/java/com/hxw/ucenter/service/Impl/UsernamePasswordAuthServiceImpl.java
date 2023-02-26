package com.hxw.ucenter.service.Impl;

import com.alibaba.nacos.api.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxw.ucenter.feignclient.CheckCodeClient;
import com.hxw.ucenter.mapper.XcUserMapper;
import com.hxw.ucenter.model.dto.AuthParamsDto;
import com.hxw.ucenter.model.dto.XcUserExt;
import com.hxw.ucenter.model.po.XcUser;
import com.hxw.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service("password_authService")
public class UsernamePasswordAuthServiceImpl implements AuthService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Resource
    XcUserMapper xcUserMapper;
    @Resource
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //得到验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)) {
            throw new RuntimeException("验证码为空");
        }
        //验证码校验，远程调用验证码验证的接口

        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);

        if (verify == null || !verify) {
            throw new RuntimeException("验证码验证错误或验证码验证失败");
        }
        //密码校验
        //查询数据库
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(wrapper);
        if (xcUser == null) {
            throw new RuntimeException("用户账号不存在");
        }
        //自己来比对密码

        String passwordInput = authParamsDto.getPassword();
        String passwordDB = xcUser.getPassword();
        boolean matches = passwordEncoder.matches(passwordInput, passwordDB);
        if (!matches) {
            throw new RuntimeException("账号或密码错误！！！！");
        }


        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);


        return xcUserExt;
    }
}
