package com.hxw.service.jobhandler;


import com.alibaba.fastjson.JSON;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.messagesdk.model.po.MqMessage;
import com.hxw.messagesdk.service.MessageProcessAbstract;
import com.hxw.messagesdk.service.MqMessageService;
import com.hxw.model.po.CoursePublish;
import com.hxw.service.CoursePublishService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {


    @Autowired
    private CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 5, 60);
    }

    //课程发布的执行逻辑
    @Override
    public boolean execute(MqMessage mqMessage) {

        log.debug("开始执行任务:{}", mqMessage.getBusinessKey1());
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //将课程信息静态化 ,将静态页面上传到minio
        generateCourseHtml(mqMessage, courseId);
        //将课程信息上传到索引库

        saveCourseIndex(mqMessage, courseId);
        //存储到redis
        saveRedisCache(mqMessage,courseId);

        return true;
    }

    //课程静态化
    public void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始课程静态化:{}", courseId);

        //生成HTML文件

        Long id = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        int stageOne = mqMessageService.getStageOne(id);
        //判断任务是否完成
        if (stageOne > 0) {
            log.debug("当前任务是页面静态化，任务已经完成了:{}", mqMessage);
            return;
        }
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("课程静态化异常");
        }

        //将HTML上传到minio系统
        coursePublishService.uploadCourseHtml(courseId, file);

        //保存第一阶段静态状态
        mqMessageService.completedStageOne(id);


    }


    //上传到课程索引
    public void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        int stageTwo = mqMessageService.getStageTwo(id);
        //判断任务是否完成
        if (stageTwo > 0) {
            log.debug("当前任务是创建课程索引，任务已经完成了:{}", mqMessage);
            return;
        }
        Boolean aBoolean = coursePublishService.saveCourseIndex(courseId);

        //保存第二阶段静态状态
        mqMessageService.completedStageTwo(id);


    }

    public void saveRedisCache(MqMessage mqMessage, Long courseId){
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        int stageThree = mqMessageService.getStageTwo(id);
        //判断任务是否完成
        if (stageThree > 0) {
            log.debug("当前任务是存入redis，任务已经完成了:{}", mqMessage);
            return;
        }
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        //完成第三阶段
        mqMessageService.completedStageThree(id);
    }

}
