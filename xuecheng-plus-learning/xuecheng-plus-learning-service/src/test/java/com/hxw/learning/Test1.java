package com.hxw.learning;

import com.hxw.base.model.PageResult;
import com.hxw.learning.feignclient.ContentServiceClient;
import com.hxw.learning.model.dto.MyCourseTableParams;
import com.hxw.learning.model.po.XcCourseTables;
import com.hxw.learning.service.MyCourseTablesService;
import com.hxw.model.po.CoursePublish;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class Test1 {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Test
    public void test() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(2L);
        System.out.println(coursepublish);
    }

    @Test
    public void test2() {
        MyCourseTableParams myCourseTableParams = new MyCourseTableParams();
        myCourseTableParams.setUserId("52");
        PageResult<XcCourseTables> mycourestabls = myCourseTablesService.mycourestabls(myCourseTableParams);
        System.out.println(mycourestabls);
    }

}
