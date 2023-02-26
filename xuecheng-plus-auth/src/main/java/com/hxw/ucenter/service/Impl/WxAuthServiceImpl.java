package com.hxw.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxw.ucenter.mapper.XcUserMapper;
import com.hxw.ucenter.mapper.XcUserRoleMapper;
import com.hxw.ucenter.model.dto.AuthParamsDto;
import com.hxw.ucenter.model.dto.XcUserExt;
import com.hxw.ucenter.model.po.XcUser;
import com.hxw.ucenter.model.po.XcUserRole;
import com.hxw.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("wx_authService")
public class WxAuthServiceImpl implements AuthService {

    @Autowired
    WxAuthServiceImpl proxy;
    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;
    @Resource
    XcUserMapper xcUserMapper;
    @Resource
    XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    private RestTemplate restTemplate;

    public XcUser wxAuth(String code) {
        //获取access_token
        Map<String, String> token = getAccess_token(code);
        if (token == null) {
            return null;
        }
        //获取用户信息
        String accessToken = token.get("access_token");
        String openid = token.get("openid");

        Map<String, String> userinfo = getUserinfo(accessToken, openid);
        if (userinfo == null) {
            return null;
        }
        //添加用户到数据库
        return proxy.addWxUser(userinfo);
    }


    //请求微信去获取令牌

    /**
     * 微信接口返回的结果
     * 申请访问令牌,响应示例
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */

    private Map<String, String> getAccess_token(String code) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String wxUrl = String.format(wxUrl_template, appid, secret, code);
        ResponseEntity<String> response = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        return JSON.parseObject(response.getBody(), Map.class);
    }


    /**
     * 获取用户信息，示例如下：
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/
     * g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0
     * ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getUserinfo(String access_token, String openid) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/" +
                "userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, access_token, openid);
        ResponseEntity<String> response = restTemplate.exchange(wxUrl, HttpMethod.GET, null, String.class);
        return JSON.parseObject(response.getBody(), Map.class);

    }

    @Transactional
    public XcUser addWxUser(Map userInfo_map) {
        String unionid = (String) userInfo_map.get("unionid");
        //检查数据库中是否存在此unionid的用户
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getWxUnionid, unionid));
        if (xcUser != null) {
            //该用户在系统已将存在了
            return xcUser;
        }
        xcUser = new XcUser();
        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionid);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfo_map.get("nickname").toString());
        xcUser.setUserpic(userInfo_map.get("headimgurl").toString());
        xcUser.setName(userInfo_map.get("nickname").toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);


        return xcUser;

    }

    //微信认证方法
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //获取用户账号
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getUsername, username));
        if (xcUser == null) {
            throw new RuntimeException("用户不存在！！！");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
