package com.hxw.media.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxw.media.mapper.MediaFilesMapper;
import com.hxw.media.mapper.MediaProcessHistoryMapper;
import com.hxw.media.mapper.MediaProcessMapper;
import com.hxw.media.model.po.MediaFiles;
import com.hxw.media.model.po.MediaProcess;
import com.hxw.media.model.po.MediaProcessHistory;
import com.hxw.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {


    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;
    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查询任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            log.debug("更新任务，此时任务{}为空", taskId);
            return;
        }
        LambdaQueryWrapper<MediaProcess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaProcess::getFileId, taskId);
        //任务失败
        if ("3".equals(status)) {
            MediaProcess process = new MediaProcess();
            process.setStatus("3");
            process.setErrormsg(errorMsg);
            mediaProcessMapper.update(process, wrapper);
            return;
        }
        //更新任务,处理成功
        if ("2".equals(status)) {
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.updateById(mediaProcess);

            //更新文件表中的url字段
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }
        //处理成功将任务添加到历史记录表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistory.setId(null);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //处理成功将待处理的表删除
        mediaProcessMapper.deleteById(taskId);
    }

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }
}
