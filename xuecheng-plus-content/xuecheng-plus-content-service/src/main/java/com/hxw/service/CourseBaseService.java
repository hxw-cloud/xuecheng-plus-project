package com.hxw.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.model.dto.AddCourseDto;
import com.hxw.model.dto.CourseBaseInfoDto;
import com.hxw.model.dto.EditCourseDto;
import com.hxw.model.dto.QueryCourseParamsDto;
import com.hxw.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-01-23
 */

public interface CourseBaseService extends IService<CourseBase> {

    /**
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 查询条件
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams,
                                                      QueryCourseParamsDto queryCourseParamsDto);

    /**
     * @param companyId    机构Id
     * @param addCourseDto 添加的课程信息
     * @return 课程信息包括基本信息，营销信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * 根据id查信息
     *
     * @param id
     * @return
     */

    public CourseBaseInfoDto getCourseBaseInfo(Long id);

    /**
     * 修改课程信息
     *
     * @param companyId     机构Id
     * @param editCourseDto 修改信息
     * @return
     */

    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

}
