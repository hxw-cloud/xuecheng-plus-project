package com.hxw.ucenter.service;

import com.hxw.ucenter.model.dto.AuthParamsDto;
import com.hxw.ucenter.model.dto.XcUserExt;

/**
 * 认证接口
 */
public interface AuthService {


    /**
     * @param authParamsDto 认证参数
     * @return com.hxw.ucenter.model.po.XcUser 用户信息
     * @description 认证方法
     */
    XcUserExt execute(AuthParamsDto authParamsDto);


}
