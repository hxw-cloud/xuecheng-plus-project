package com.hxw.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Mr.M
 * @version 1.0
 * @description 安全管理配置
 * @date 2022/9/26 20:53
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    public static void main(String[] args) {
        String password = "hxw";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        for (int i = 0; i < 10; i++) {
        //生成加密
//            String encode = encoder.encode(password);
//            System.out.println(encode);
//        }

        //判断 密码是否正确
        boolean matches = encoder.matches(password, "$2a$10$W2fRkUSoTi9wO6zNqbef.O2GuzFihpg/XEm.7poOxmpSUnhG.7VKK");
        System.out.println(matches);
    }

    //使用自己定义DaoAuthenticationProviderCustom来代替框架的DaoAuthenticationProvider
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }


    //配置用户信息服务
//    @Bean
//    public UserDetailsService userDetailsService() {
//        //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
//        manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
//        return manager;
//    }

    //使用自己定义的DaoAuthenticationProviderCustom来比较密码
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
//        //密码为明文方式
//        return NoOpPasswordEncoder.getInstance();
        //单向的hash加密算法
        return new BCryptPasswordEncoder();
    }

    //配置安全拦截机制
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/r/**").authenticated()//访问/r开始的请求需要认证通过
                .anyRequest().permitAll()//其它请求全部放行
                .and()
                .formLogin().successForwardUrl("/login-success");//登录成功跳转到/login-success
    }


}
