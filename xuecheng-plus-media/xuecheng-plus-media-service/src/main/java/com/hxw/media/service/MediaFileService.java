package com.hxw.media.service;

import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.base.model.RestResponse;
import com.hxw.media.model.dto.QueryMediaParamsDto;
import com.hxw.media.model.dto.UploadFileParamsDto;
import com.hxw.media.model.dto.UploadFileResultDto;
import com.hxw.media.model.po.MediaFiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

/**
 * @version 1.0
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.hxw.base.model.PageResult<com.hxw.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,
                                                  PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto);


    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件信息
     * @param bytes               文件字节数组
     * @param folder              桶下边的子目录
     * @param objectName          对象名称
     * @return com.hxw.media.model.dto.UploadFileResultDto
     * @description 上传文件的通用接口
     */
    public UploadFileResultDto uploadFile(Long companyId,
                                          UploadFileParamsDto uploadFileParamsDto,
                                          byte[] bytes,
                                          String folder,
                                          String objectName);


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return com.hxw.media.model.po.MediaFiles
     * @description 将文件信息添加到文件表
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName);


    /**
     * @param fileMd5 文件的md5
     * @return com.hxw.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查文件是否存在
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return com.hxw.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查分块是否存在
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);


    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param bytes   文件字节
     * @return com.hxw.base.model.RestResponse
     * @description 上传分块
     */
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.hxw.base.model.RestResponse
     * @description 合并分块
     */
    public RestResponse mergeChunks(Long companyId,
                                    String fileMd5,
                                    int chunkTotal,
                                    UploadFileParamsDto uploadFileParamsDto);


    /**
     * 根据id查询url
     *
     * @param id id
     * @return 文件信息
     */

    public MediaFiles getFileById(String id);

    /**
     * 下载文件到本地
     *
     * @param file       传入下载到本地的路径
     * @param objectName 文件路径
     * @return 下载的文件
     */
    public File downloadFileFromMinIO(File file, String bucket, String objectName);

    /**
     * 上传文件
     *
     * @param filePath   文件路径
     * @param bucket     桶
     * @param objectName 文件在minio的路径
     */

    public void addMediaFilesToMinIO(String filePath,
                                     String bucket,
                                     String objectName
    );
}
