package com.hxw.api;

import com.hxw.model.dto.CourseCategoryTreeDto;
import com.hxw.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "课程分类相关接口", tags = "课程分类相关接口")
@RestController
@CrossOrigin(origins = "*")
public class CourseCategoryController {


    @Autowired
    private CourseCategoryService courseCategoryService;


    @ApiOperation("课程分类查询接口")
    @GetMapping("course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
