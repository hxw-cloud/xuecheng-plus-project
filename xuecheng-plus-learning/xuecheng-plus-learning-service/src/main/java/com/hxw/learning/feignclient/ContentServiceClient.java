package com.hxw.learning.feignclient;

import com.hxw.learning.feignclient.factory.ContentServiceClientFallbackFactory;
import com.hxw.model.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Mr.M
 * @version 1.0
 * @description 内容管理服务远程接口
 * @date 2022/9/20 20:29
 */
@FeignClient(value = "content-api", url = "127.0.0.1:63040", fallbackFactory = ContentServiceClientFallbackFactory.class)
@RequestMapping("/content")
public interface ContentServiceClient {

    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);
}
