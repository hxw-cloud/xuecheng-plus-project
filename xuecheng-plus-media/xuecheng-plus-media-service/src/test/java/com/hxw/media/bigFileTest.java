package com.hxw.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class bigFileTest {

    @Test
    public void testChunk() throws IOException {

        //源文件
        File sourceFile = new File("D:\\Computers\\Desktop\\Aboutme.mp4");

        //分块路径
        File chunkFilePath = new File("D:\\Computers\\Desktop\\Aboutme\\");
        if (!chunkFilePath.exists()) {
            chunkFilePath.mkdirs();
        }
        //分块大小
        long chunkSize = 1024 * 1024 * 1;
        //分块数量
        long ceil = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        byte[] bytes = new byte[1024];
        //思路：使用流对象读取源文件，向分块中写入数据
        RandomAccessFile randomAccessFile = new RandomAccessFile(sourceFile, "r");
        for (int i = 0; i < ceil; i++) {
            File file = new File("D:\\Computers\\Desktop\\Aboutme\\" + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = randomAccessFile.read(bytes)) != -1) {
                    accessFile.write(bytes, 0, len);
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                accessFile.close();
            }
        }
        randomAccessFile.close();
    }


    @Test
    public void testMerge() throws IOException {
        //分块路径
        File chunkFilePath = new File("D:\\Computers\\Desktop\\Aboutme\\");
        //源文件
        File sourceFile = new File("D:\\Computers\\Desktop\\aboutme.mp4");
        //合并文件
        File merageFile = new File("D:\\Computers\\Desktop\\aboutme1.mp4");
        if (merageFile.exists()) {
            merageFile.delete();
        }
        merageFile.createNewFile();

        RandomAccessFile raf_write = new RandomAccessFile(merageFile, "rw");

        raf_write.seek(0);

        //分块列表
        File[] fileArray = chunkFilePath.listFiles();
        // 转成集合，便于排序
        List<File> fileList = Arrays.asList(fileArray);
        // 从小到大排序
//        Collections.sort(fileList, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        Collections.sort(fileList, Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
        byte[] bytes = new byte[1024];
        for (File file : fileList) {
            int len = -1;
            RandomAccessFile raf_read = new RandomAccessFile(file, "r");
            while ((len = raf_read.read(bytes)) != -1) {
                raf_write.write(bytes, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();

        //校验文件
        try (
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                FileInputStream mergeFileStream = new FileInputStream(merageFile);
        ) {
            //取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            //取出合并文件的md5进行比较
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }
        }
    }
}