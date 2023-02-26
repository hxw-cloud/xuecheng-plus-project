package com.hxw.media.api;


import com.hxw.base.model.RestResponse;
import com.hxw.media.model.dto.UploadFileParamsDto;
import com.hxw.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Api(value = "大文件上传接口", tags = "大文件上传接口")
public class BigFilesController {


    @Autowired
    private MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        return mediaFileService.uploadChunk(fileMd5, chunk, file.getBytes());

    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) {
        UploadFileParamsDto dto = new UploadFileParamsDto();
        dto.setFilename(fileName);
        dto.setFileType("001002");
        dto.setTags("课程视频");
        dto.setRemark("");
//        //获取用户的身份
//        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
//        //获取用户所属的机构id
//        Long companyId = Long.valueOf(xcUser.getCompanyId());
        Long companyId = 1L;
        return mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, dto);

    }
}
