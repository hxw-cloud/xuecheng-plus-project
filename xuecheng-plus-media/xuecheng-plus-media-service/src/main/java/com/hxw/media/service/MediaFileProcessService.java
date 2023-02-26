package com.hxw.media.service;

import com.hxw.media.model.po.MediaProcess;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MediaFileProcessService {

    /**
     * @param status   处理结果，2:成功3失败
     * @param fileId   文件id
     * @param url      文件访问url
     * @param errorMsg 失败原因
     * @return void
     * @description 将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
     */

    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

    /**
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return java.util.List<com.hxw.media.model.po.MediaProcess>
     * @description 获取待处理任务
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


}