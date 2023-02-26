package com.hxw.utils;

import com.alibaba.fastjson.JSON;
import com.hxw.base.exception.XueChengPlusException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@Data
public class SecurityUtils {


    public static XcUser getXcUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof String) {
            String json = (String) principal;
            try {
                return JSON.parseObject(json, XcUser.class);
            } catch (Exception e) {
                log.debug("解析jwt中的用户身份，无法解析成xcUser:{}", json);
                XueChengPlusException.cast("解析用户身份信息失败+" + e.getMessage());
            }

        }
        return null;
    }


    @Data
    public static class XcUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;
        private String nickname;
        private String wxUnionid;
        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;


    }
}
