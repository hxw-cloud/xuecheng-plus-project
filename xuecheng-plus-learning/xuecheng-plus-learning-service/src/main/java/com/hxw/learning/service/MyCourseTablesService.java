package com.hxw.learning.service;

import com.hxw.base.model.PageResult;
import com.hxw.learning.model.dto.MyCourseTableParams;
import com.hxw.learning.model.dto.XcChooseCourseDto;
import com.hxw.learning.model.dto.XcCourseTablesDto;
import com.hxw.learning.model.po.XcCourseTables;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表service接口
 * @date 2022/10/2 16:07
 */
public interface MyCourseTablesService {

    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @description 判断学习资格
     * @author Mr.M
     * @date 2022/10/3 7:37
     */
    public XcCourseTablesDto getLeanringStatus(String userId, Long courseId);


    public boolean saveChooseCourseStauts(String choosecourseId);

    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);

}
