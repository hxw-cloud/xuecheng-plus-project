package com.hxw.feignclient.factory;

import com.hxw.feignclient.SearchFeignClient;
import com.hxw.feignclient.model.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SearchFeignClientFallbackFactory implements FallbackFactory<SearchFeignClient> {
    @Override
    public SearchFeignClient create(Throwable throwable) {
        return new SearchFeignClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                throwable.printStackTrace();
                log.debug("调用搜索发生熔断走降级，熔断异常{}", throwable.getMessage());
                return false;
            }
        };
    }
}
