package com.hxw.learning.feignclient;

import com.hxw.base.model.RestResponse;
import com.hxw.learning.feignclient.factory.MediaServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资管理服务远程接口
 * @date 2022/9/20 20:29
 */
@FeignClient(value = "media-api", url = "127.0.0.1:63050", fallbackFactory = MediaServiceClientFallbackFactory.class)
@RequestMapping("/media")
public interface MediaServiceClient {

    @GetMapping("/open/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);

}
