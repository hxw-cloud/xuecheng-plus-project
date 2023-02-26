package com.hxw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hxw.model.dto.CourseCategoryTreeDto;
import com.hxw.model.po.CourseCategory;

import java.util.List;


/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-01-24
 */
public interface CourseCategoryService extends IService<CourseCategory> {


    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
