package com.hxw.feignclient;

import com.hxw.config.MultipartSupportConfig;
import com.hxw.feignclient.factory.MediaFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "media-api", url = "http://127.0.0.1:63050",
        configuration = MultipartSupportConfig.class,
        fallbackFactory = MediaFeignClientFallbackFactory.class)
public interface MediaFeignClient {


    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestPart("filedata") MultipartFile upload,
                             @RequestParam(value = "folder", required = false) String folder,
                             @RequestParam(value = "objectName", required = false) String objectName);


}
