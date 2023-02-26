package com.hxw.media.api;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.model.RestResponse;
import com.hxw.media.model.po.MediaFiles;
import com.hxw.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
@CrossOrigin(origins = "*")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {

        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());

    }


}