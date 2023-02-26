package com.hxw;


import com.hxw.config.MultipartSupportConfig;
import com.hxw.feignclient.MediaFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@SpringBootTest
public class FeignUpladeTest {
    @Autowired
    MediaFeignClient mediaFeignClient;


    @Test
    void test() {
        File file = new File("D:\\Computers\\Desktop\\tools\\test.html");

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);


        String course = mediaFeignClient.uploadFile(multipartFile, "course", "test.html");
        System.out.println(course);
    }


}
