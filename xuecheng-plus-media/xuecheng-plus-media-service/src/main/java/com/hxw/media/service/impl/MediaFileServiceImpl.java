package com.hxw.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.base.model.RestResponse;
import com.hxw.media.mapper.MediaFilesMapper;
import com.hxw.media.mapper.MediaProcessMapper;
import com.hxw.media.model.dto.QueryMediaParamsDto;
import com.hxw.media.model.dto.UploadFileParamsDto;
import com.hxw.media.model.dto.UploadFileResultDto;
import com.hxw.media.model.po.MediaFiles;
import com.hxw.media.model.po.MediaProcess;
import com.hxw.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFileService currentProxy;
    @Autowired
    private MinioClient minioClient;
    @Resource
    private MediaProcessMapper mediaProcessMapper;
    @Resource
    private MediaFilesMapper mediaFilesMapper;
    @Value("${minio.bucket.files}")
    private String bucket_files;
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,
                                                  PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto) {

        //????????????????????????
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(queryMediaParamsDto.getFilename()))
            queryWrapper.like(MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        if (!StringUtils.isEmpty(queryMediaParamsDto.getFileType()))
            queryWrapper.eq(MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        //????????????
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // ??????????????????????????????
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // ??????????????????
        List<MediaFiles> list = pageResult.getRecords();
        // ??????????????????
        long total = pageResult.getTotal();
        // ???????????????
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId,
                                          UploadFileParamsDto uploadFileParamsDto,
                                          byte[] bytes,
                                          String folder,
                                          String objectName) {


        //???????????????md5???
        String fileMd5 = DigestUtils.md5Hex(bytes);

        if (StringUtils.isEmpty(folder)) {
            //??????????????????????????? ?????????????????????
            folder = getFileFolder(new Date(), true, true, true);
        } else if (!folder.contains("/")) {
            folder = folder + "/";
        }
        //????????????
        String filename = uploadFileParamsDto.getFilename();

        if (StringUtils.isEmpty(objectName)) {
            //??????objectName????????????????????????md5??????objectName
            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
        }

        objectName = folder + objectName;

        try {
            //?????????minio
            addMediaFilesToMinIO(bytes,
                    bucket_files,
                    objectName);
            //??????????????????
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(
                    companyId, fileMd5,
                    uploadFileParamsDto,
                    bucket_files, objectName);
            //??????????????????
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;


        } catch (Exception e) {
            log.debug("?????????????????????{}", e.getMessage());
        }

        return null;
    }

    //????????????????????????
    private String getFileFolder(Date date, boolean year,
                                 boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //???????????????????????????
        String dateString = sdf.format(new Date());
        //?????????????????????
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }


    /**
     * ???????????????minio
     *
     * @param filePath   ????????????
     * @param bucket     ???
     * @param objectName ?????????
     */
    public void addMediaFilesToMinIO(String filePath,
                                     String bucket,
                                     String objectName
    ) {

        //????????????????????????????????????
        String contentType = getMimeTypeByExtension(objectName);
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//????????????????????????????????????
                    .filename(filePath)
                    .contentType(contentType)
                    .build();
            //??????
            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            log.debug("????????????!!!!!");
            XueChengPlusException.cast("????????????!!!!!!!!!");
        }

    }

    /**
     * @param bytes      ??????????????????
     * @param bucket     ???
     * @param objectName ????????????
     * @return void
     * @description ???????????????minIO
     */
    private void addMediaFilesToMinIO(byte[] bytes,
                                      String bucket,
                                      String objectName) {
        //????????????????????????????????????
        String contentType = getMimeTypeByExtension(objectName);
        //?????????
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    //InputStream stream, long objectSize ????????????,
                    // long partSize ????????????(-1??????5M,??????????????????5T?????????10000)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("?????????????????????????????????");
        }
    }

    /**
     * @param companyId           ??????id
     * @param fileMd5             ??????md5???
     * @param uploadFileParamsDto ?????????????????????
     * @param bucket              ???
     * @param objectName          ????????????
     * @return com.hxw.media.model.po.MediaFiles
     * @description ?????????????????????????????????
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket, String objectName) {
        //????????????????????????
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //??????????????????
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFilePath(objectName);
            String contentType = getMimeTypeByExtension(objectName);

            if (contentType.contains("image") || contentType.contains("mp4"))
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //??????????????????????????????
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XueChengPlusException.cast("????????????????????????");
            }
            if (contentType.equals("video/x-msvideo")) {
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles, mediaProcess);
                mediaProcess.setStatus("1");
                int i = mediaProcessMapper.insert(mediaProcess);
                if (i < 0) {
                    XueChengPlusException.cast("????????????????????????");
                }
            }

        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }

        GetObjectArgs objectArgs = GetObjectArgs
                .builder().bucket(mediaFiles.getBucket())
                .object(mediaFiles.getFilePath()).build();
        try {
            InputStream inputStream = minioClient.getObject(objectArgs);
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String path = getChunkFileFolderPath(fileMd5);
        String filePath = path + chunkIndex;

        GetObjectArgs objectArgs = GetObjectArgs
                .builder().bucket(bucket_videofiles)
                .object(filePath).build();
        try {
            InputStream inputStream = minioClient.getObject(objectArgs);
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }

        return RestResponse.success(true);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        String path = getChunkFileFolderPath(fileMd5);
        String filePath = path + chunk;
        try {
            addMediaFilesToMinIO(bytes, bucket_videofiles, filePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("??????????????????:{},??????:{}", filePath, e.getMessage());
        }

        return RestResponse.success(false);
    }

    @Override
    public RestResponse mergeChunks(Long companyId,
                                    String fileMd5,
                                    int chunkTotal,
                                    UploadFileParamsDto uploadFileParamsDto) {
        //????????????
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        //?????????????????????
//        String contentType;
        String filename = uploadFileParamsDto.getFilename();
        String fileType = null;
        if (filename.contains("."))
            fileType = filename.substring(filename.lastIndexOf("."));
        //????????????
        File file = null;
        try {
            try {
                file = File.createTempFile("merge", fileType);
            } catch (Exception e) {
                e.printStackTrace();
                XueChengPlusException.cast("??????????????????????????????!!!!!");
            }
            //????????????
            try (
                    RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
            ) {

                byte[] bytes = new byte[1024];
                for (File chunkFile : chunkFiles) {
                    RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                    int len = -1;
                    while ((len = raf_read.read(bytes)) != -1) {
                        raf_write.write(bytes, 0, len);
                    }
                    raf_read.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                XueChengPlusException.cast("??????????????????!!!!!");
            }
            //????????????
            try (
                    FileInputStream inputStream = new FileInputStream(file);
            ) {
                String md5Hex = DigestUtils.md5Hex(inputStream);
                if (!fileMd5.equals(md5Hex)) {
                    log.debug("MD5????????????");
                    XueChengPlusException.cast("MD5????????????");
                }
            } catch (Exception e) {
                XueChengPlusException.cast("????????????????????????!!!!!");
            }
            //????????????????????????
            String path = getFilePathByMd5(fileMd5, fileType);
            //???????????????minio
            addMediaFilesToMinIO(file.getAbsolutePath(), bucket_videofiles, path);
            //???????????????????????????
            uploadFileParamsDto.setFileSize(file.length());
            currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, path);

            return RestResponse.success(true);
        } finally {
            if (chunkFiles != null) {
                for (File chunkFile : chunkFiles) {
                    if (chunkFile.exists()) {
                        chunkFile.delete();
                    }
                }
            }
            if (file != null) {
                file.delete();
            }
        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles files = mediaFilesMapper.selectById(id);
        if (files == null) {
            XueChengPlusException.cast("???????????????");
        }
        String url = files.getUrl();
        if (StringUtils.isEmpty(url)) {

            XueChengPlusException.cast("??????????????????");
        }
        return files;
    }

    /**
     * ?????????????????????
     *
     * @param fileMd5
     * @param chunkTotal
     * @return ????????????
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {

        String path = getChunkFileFolderPath(fileMd5);

        File[] files = new File[chunkTotal];
        for (int i = 0; i < chunkTotal; i++) {
            String filePath = path + i;
            File file = null;
            try {
                file = File.createTempFile("huxinwei", null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("??????????????????????????????" + e.getMessage());
            }
            file = downloadFileFromMinIO(file, bucket_videofiles, filePath);
            files[i] = file;

        }

        return files;
    }


    //???????????????????????????
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5
                .substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    public File downloadFileFromMinIO(File file, String bucket, String objectName) {
        try (
                InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                );
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream, outputStream);
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("???????????????????????????????????????");
        }
        return null;
    }

    private String getMimeTypeByExtension(String objectName) {
        //?????????
        String extension = null;
        if (objectName.contains(".")) {
            extension = objectName.substring(objectName.lastIndexOf("."));
        }
        //????????????????????????????????????
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }
}
