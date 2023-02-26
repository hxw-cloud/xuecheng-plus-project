package com.hxw.learning.api;

import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.model.PageResult;
import com.hxw.learning.model.dto.MyCourseTableParams;
import com.hxw.learning.model.dto.XcChooseCourseDto;
import com.hxw.learning.model.dto.XcCourseTablesDto;
import com.hxw.learning.model.po.XcCourseTables;
import com.hxw.learning.service.MyCourseTablesService;
import com.hxw.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/2 14:52
 */
@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Resource
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return myCourseTablesService.addChooseCourse(userId, courseId);

    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return myCourseTablesService.getLeanringStatus(userId, courseId);

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        params.setUserId(userId);
        return myCourseTablesService.mycourestabls(params);

    }


}
