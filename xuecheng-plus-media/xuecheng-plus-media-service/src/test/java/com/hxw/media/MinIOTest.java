package com.hxw.media;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试minio上传文件、删除文件、查询文件
 * @date 2022/10/13 14:42
 */
public class MinIOTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://8.130.29.229:9090")
                    .credentials("admin", "hu123321")
                    .build();


    @Test
    public void upload() {

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("debug.log")//同一个桶内对象名不能重复
                    .filename("D:\\Computers\\Downloads\\debug.log")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败");
        }


    }

    //指定桶内的子目录
    @Test
    public void upload2() {

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/debug.log")//同一个桶内对象名不能重复
                    .filename("D:\\Computers\\Downloads\\debug.log")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败");
        }


    }

    //删除文件
    @Test
    public void delete() {

        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("test/debug.log").build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
        }

    }

    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("debug.log").build();
        try (
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\Computers\\Downloads\\debug-1.log"));
        ) {

            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (Exception e) {
        }

    }

}
