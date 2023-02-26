package com.hxw.api;

import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.model.dto.AddCourseDto;
import com.hxw.model.dto.CourseBaseInfoDto;
import com.hxw.model.dto.EditCourseDto;
import com.hxw.model.dto.QueryCourseParamsDto;
import com.hxw.model.po.CourseBase;
import com.hxw.service.CourseBaseService;
import com.hxw.service.CourseMarketService;
import com.hxw.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
@CrossOrigin(origins = "*")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseService courseBaseService;

    @Resource
    private CourseMarketService courseMarketService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('course_find_list')")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody QueryCourseParamsDto queryCourseParams) {

        //获取用户的身份
        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
        //获取用户所属的机构id
        Long companyId = Long.valueOf(xcUser.getCompanyId());
        //实现细粒度授权，使本机构只能修改本机构的课程

        PageResult<CourseBase> pageResult = courseBaseService.queryCourseBaseList(companyId, pageParams, queryCourseParams);
        return pageResult;

    }


    @ApiOperation("新增加课程")
    @PostMapping("/course")
    public CourseBaseInfoDto creatCourseBase(@RequestBody @Validated AddCourseDto addCourseDto) {
        //获取用户的身份
        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
        //获取用户所属的机构id
        Long companyId = Long.valueOf(xcUser.getCompanyId());
        CourseBaseInfoDto base = courseBaseService.createCourseBase(companyId, addCourseDto);

        return base;
    }


    @GetMapping("course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId) {
        return courseBaseService.getCourseBaseInfo(courseId);
    }


    @PutMapping("course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody EditCourseDto editCourseDto) {
//获取用户的身份
        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
        //获取用户所属的机构id
        Long companyId = Long.valueOf(xcUser.getCompanyId());
        CourseBaseInfoDto dto = courseBaseService.updateCourseBase(companyId, editCourseDto);
        return dto;
    }


    @DeleteMapping("course/{courseId}")
    public void removeCourseBase(@PathVariable("courseId") Long id) {
        boolean b = courseBaseService.removeById(id);
        boolean b1 = courseMarketService.removeById(id);
    }


}