package com.hxw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.model.dto.CourseCategoryTreeDto;
import com.hxw.model.po.CourseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
