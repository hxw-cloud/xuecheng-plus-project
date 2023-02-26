package com.hxw.api;

import com.alibaba.fastjson.JSON;
import com.hxw.model.dto.CourseBaseInfoDto;
import com.hxw.model.dto.CoursePreviewDto;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.model.po.CoursePublish;
import com.hxw.service.CoursePublishService;
import com.hxw.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@Api(value = "课程预览发布接口", tags = "课程预览发布接口")
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {


        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }


    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        //获取用户的身份
        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
        //获取用户所属的机构id
        Long companyId = Long.valueOf(xcUser.getCompanyId());
        coursePublishService.commitAudit(companyId, courseId);
    }


    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        //获取用户的身份
        SecurityUtils.XcUser xcUser = SecurityUtils.getXcUser();
        //获取用户所属的机构id
        Long companyId = Long.valueOf(xcUser.getCompanyId());
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布信息
//        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if (coursePublish == null) {
            return null;
        }
        //课程发布状态
        String status = coursePublish.getStatus();
        if (status.equals("203002")) {
            return coursePublish;
        }
        //课程下线返回null
        return null;

    }

    @ApiOperation("获取课程全部信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {

        //查询课程发布表
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if (coursePublish == null) {
            return null;
        }
        //基本信息和课程营销信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);

        //封装课程信息

        String teachplanJson = coursePublish.getTeachplan();
        List<TeachPlanDto> teachPlan = JSON.parseArray(teachplanJson, TeachPlanDto.class);
        //要封装的对象
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setTeachplans(teachPlan);
        coursePreviewDto.setCourseBase(courseBase);


        return coursePreviewDto;
    }
}