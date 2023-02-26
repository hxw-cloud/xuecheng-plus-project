package com.hxw.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.learning.model.dto.MyCourseTableItemDto;
import com.hxw.learning.model.dto.MyCourseTableParams;
import com.hxw.learning.model.po.XcCourseTables;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface XcCourseTablesMapper extends BaseMapper<XcCourseTables> {

    public List<MyCourseTableItemDto> myCourseTables(MyCourseTableParams params);

    public int myCourseTablesCount(MyCourseTableParams params);

}
