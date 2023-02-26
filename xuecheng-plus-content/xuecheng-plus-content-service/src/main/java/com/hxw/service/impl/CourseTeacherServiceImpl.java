package com.hxw.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxw.mapper.CourseTeacherMapper;
import com.hxw.model.po.CourseTeacher;
import com.hxw.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

}
