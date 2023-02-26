package com.hxw.media.service.jobhandler;


import com.hxw.base.utils.Mp4VideoUtil;
import com.hxw.media.model.po.MediaProcess;
import com.hxw.media.service.MediaFileProcessService;
import com.hxw.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTaskXxlJob {

    @Value("${videoprocess.ffmpegpath}")
    private String videoPath;
    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    /**
     * 分片广播任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();

        //查询待处理任务
        List<MediaProcess> list = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);

        if (list == null || list.size() < 0) {
            log.debug("查询到的视频为空");
            return;
        }
        //启动多线程处理
        //要处理的任务数
        int size = list.size();
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //开启的线程池
        ExecutorService pool = Executors.newFixedThreadPool(size);
        //遍历，将list放入线程池
        list.forEach(mediaProcess -> {
            pool.execute(() -> {
                //视频处理状态
                String status = mediaProcess.getStatus();
                //保证幂等性
                if ("2".equals(status)) {
                    log.debug("视频被处理过了！！！！视频信息{}", mediaProcess);
                    //计数器减一
                    countDownLatch.countDown();
                    return;
                }
                //桶，存储路径，原始文件MD5值,文件名
                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                String fileId = mediaProcess.getFileId();
                String filename = mediaProcess.getFilename();
                //将原始视频下载到本地
                //先创建临时文件，为原始的视频文件
                File originalVideo = null;
                //处理结束的mp4文件
                File mp4Video = null;
                try {
                    originalVideo = File.createTempFile("original", null);
                    mp4Video = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("下载待处理的原始文件前创建临时文件失败");
                    //计数器减一
                    countDownLatch.countDown();
                    return;
                }
                try {
                    //视频文件下载到本地
                    mediaFileService.downloadFileFromMinIO(originalVideo, bucket, filePath);
                } catch (Exception e) {
                    log.error("下载原始文件过程出错{},文件信息{}", e.getMessage(), mediaProcess);
                    //计数器减一
                    countDownLatch.countDown();
                    return;
                }
                //将视频转换为MP4

                Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(videoPath,
                        originalVideo.getAbsolutePath(),
                        fileId + ".mp4",
                        mp4Video.getAbsolutePath());
                String mp4 = mp4VideoUtil.generateMp4();
                String newStatus = "3";
                String url = null;
                if ("success".equals(mp4)) {
                    //上传到minio
                    String objectName = getFilePathByMd5(fileId, ".mp4");
                    try {
                        mediaFileService.addMediaFilesToMinIO(
                                mp4Video.getAbsolutePath(),
                                bucket,
                                objectName);
                    } catch (Exception e) {
                        log.error("上传文件失败！！！！错误信息{}", e.getMessage());
                        //计数器减一
                        countDownLatch.countDown();
                        return;
                    }
                    newStatus = "2";
                    url = "/" + bucket + "/" + objectName;
                }
                try {
                    //记录处理结果
                    mediaFileProcessService
                            .saveProcessFinishStatus(mediaProcess.getId(), newStatus, fileId, url, mp4);
                } catch (Exception e) {
                    log.error("保存到数据库出错{}", e.getMessage());
                    //计数器减一
                    countDownLatch.countDown();
                    return;
                }
                //计数器减一
                countDownLatch.countDown();
            });
        });

        //阻塞到任务处理完成
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/"
                + fileMd5.substring(1, 2)
                + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
