package com.hxw.service;

import com.hxw.model.dto.CoursePreviewDto;
import com.hxw.model.po.CoursePublish;

import java.io.File;

public interface CoursePublishService {

    /**
     * @param courseId 课程id
     * @return dto.model.com.hxw.CoursePreviewDto
     * @description 获取课程预览信息
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);


    /**
     * @param courseId 课程id
     * @return void
     * @description 提交审核
     */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口
     * @author Mr.M
     * @date 2022/9/20 16:23
     */
    public void publish(Long companyId, Long courseId);

    /**
     * @param courseId 课程id
     * @return File 静态化文件
     * @description 课程静态化
     */
    public File generateCourseHtml(Long courseId);

    /**
     * @param file 静态化文件
     * @return void
     * @description 上传课程静态化页面
     */
    public void uploadCourseHtml(Long courseId, File file);


    /**
     * 上传到索引
     *
     * @param courseId 上传的任务ID
     * @return 是否成功
     */
    public Boolean saveCourseIndex(Long courseId);



    public CoursePublish getCoursePublish(Long courseId);


    public CoursePublish getCoursePublishCache(Long courseId);
}
