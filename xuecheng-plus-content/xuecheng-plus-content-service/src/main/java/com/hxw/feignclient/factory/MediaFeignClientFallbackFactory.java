package com.hxw.feignclient.factory;

import com.hxw.feignclient.MediaFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MediaFeignClientFallbackFactory implements FallbackFactory<MediaFeignClient> {
    @Override
    public MediaFeignClient create(Throwable throwable) {
        return new MediaFeignClient() {
            @Override
            public String uploadFile(MultipartFile upload, String folder, String objectName) {
                log.debug("调用媒资文件上传服务时发生熔断，异常信息:{}", throwable.getMessage());

                return null;
            }
        };
    }
}
