package com.hxw.feignclient;

import com.hxw.feignclient.factory.SearchFeignClientFallbackFactory;
import com.hxw.feignclient.model.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "search", url = "http://127.0.0.1:63080",
        fallbackFactory = SearchFeignClientFallbackFactory.class)
public interface SearchFeignClient {


    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);


}
