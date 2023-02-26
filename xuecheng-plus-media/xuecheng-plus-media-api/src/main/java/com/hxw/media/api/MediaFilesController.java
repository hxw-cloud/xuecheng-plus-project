package com.hxw.media.api;

import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.base.model.RestResponse;
import com.hxw.media.model.dto.QueryMediaParamsDto;
import com.hxw.media.model.dto.UploadFileParamsDto;
import com.hxw.media.model.dto.UploadFileResultDto;
import com.hxw.media.model.po.MediaFiles;
import com.hxw.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@CrossOrigin(origins = "*")
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams,
                                       @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        //获取用户的身份
//        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
//        //获取用户所属的机构id
//        if (xcUser.getCompanyId() == null) {
//            XueChengPlusException.cast("机构不存在！！！");
//        }
//        Long companyId = Long.valueOf(xcUser.getCompanyId());
        Long companyId = 1L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);

    }

    @RequestMapping(value = "/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {

        //获取用户的身份
//        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
//        //获取用户所属的机构id
//        if (xcUser.getCompanyId() == null) {
//            XueChengPlusException.cast("机构不存在！！！");
//        }
//        Long companyId = Long.valueOf(xcUser.getCompanyId());
        Long companyId = 1L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        String contentType = filedata.getContentType();
        uploadFileParamsDto.setContentType(contentType);
        uploadFileParamsDto.setFileSize(filedata.getSize());//文件大小
        if (contentType.contains("image")) {
            //是个图片
            uploadFileParamsDto.setFileType("001001");
        } else {
            uploadFileParamsDto.setFileType("001003");
        }
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());//文件名称
        UploadFileResultDto uploadFileResultDto = null;
        try {
            uploadFileResultDto = mediaFileService.uploadFile(
                    companyId,
                    uploadFileParamsDto,
                    filedata.getBytes(),
                    folder,
                    objectName);
        } catch (Exception e) {
            XueChengPlusException.cast("上传文件过程中出错");
        }

        return uploadFileResultDto;

    }


    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {

        MediaFiles file = mediaFileService.getFileById(mediaId);

        return RestResponse.success(file.getUrl());
    }
}
