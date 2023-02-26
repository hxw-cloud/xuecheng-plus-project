package com.hxw;

import com.hxw.mapper.CourseBaseMapper;
import com.hxw.model.dto.CourseCategoryTreeDto;
import com.hxw.model.po.CourseBase;
import com.hxw.service.CourseBaseService;
import com.hxw.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ServiceApplicationTests {

    @Autowired
    private CourseBaseMapper courseBaseMapper;


    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    void contextLoads() {

        CourseBase courseBase = courseBaseMapper.selectById(22l);
        System.out.println(courseBase);
    }


//    @Test
//    void contextLoads1() {
//        PageParams pageParams = new PageParams();
//        PageResult<CourseBase> list = courseBaseService
//                .queryCourseBaseList(pageParams, new QueryCourseParamsDto());
//        System.out.println("list = " + list);
//    }


    @Test
    void contextLoads2() {

        List<CourseCategoryTreeDto> dtos = courseCategoryService.queryTreeNodes("1");
        dtos.forEach(System.out::println);
    }

}
