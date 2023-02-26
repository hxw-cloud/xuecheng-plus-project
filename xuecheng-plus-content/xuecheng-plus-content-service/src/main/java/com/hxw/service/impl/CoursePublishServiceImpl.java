package com.hxw.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.utils.StringUtils;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.config.MultipartSupportConfig;
import com.hxw.feignclient.MediaFeignClient;
import com.hxw.feignclient.SearchFeignClient;
import com.hxw.feignclient.model.CourseIndex;
import com.hxw.mapper.CourseBaseMapper;
import com.hxw.mapper.CourseMarketMapper;
import com.hxw.mapper.CoursePublishMapper;
import com.hxw.mapper.CoursePublishPreMapper;
import com.hxw.messagesdk.model.po.MqMessage;
import com.hxw.messagesdk.service.MqMessageService;
import com.hxw.model.dto.CourseBaseInfoDto;
import com.hxw.model.dto.CoursePreviewDto;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.model.po.CourseBase;
import com.hxw.model.po.CourseMarket;
import com.hxw.model.po.CoursePublish;
import com.hxw.model.po.CoursePublishPre;
import com.hxw.service.CourseBaseService;
import com.hxw.service.CoursePublishService;
import com.hxw.service.TeachplanService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    SearchFeignClient searchFeignClient;
    @Autowired
    MediaFeignClient mediaFeignClient;
    @Autowired
    private CourseBaseService courseBaseService;
    @Resource
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private TeachplanService teachplanService;
    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private MqMessageService mqMessageService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);

        List<TeachPlanDto> TeachPlanDtos = teachplanService.selectTreeNodes(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(TeachPlanDtos);

        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }


        //查询课程计划信息
        List<TeachPlanDto> teachplanTree = teachplanService.selectTreeNodes(courseId);
        if (teachplanTree.size() <= 0) {
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }

        //封装数据，基本信息，营销信息，课程计划，师资
        CoursePublishPre coursePublishPre = new CoursePublishPre();

        //查询基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //将课程计划信息转化为json
        String jsonString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(jsonString);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String market = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(market);

        //课程审核状态
        coursePublishPre.setStatus("202003");

        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1 == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }


        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if (!"202004".equals(auditStatus)) {
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }


        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);


    }

    @Override
    public File generateCourseHtml(Long courseId) {
        try {

            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建临时文件
            File htmlFile = File.createTempFile("course", ".html");
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
            return htmlFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaFeignClient.uploadFile(multipartFile, "course", courseId + ".html");

        if (course == null) {
            XueChengPlusException.cast("上传到minio失败");
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {

        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //异常处理
        if (coursePublish == null) {
            XueChengPlusException.cast("发布课程不存在");
        }
        //组装数据
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);

        Boolean add = searchFeignClient.add(courseIndex);

        if (!add) {
            XueChengPlusException.cast("创建任务索引失败");
        }

        return add;
    }

    public CoursePublish getCoursePublish(Long courseId) {

        return coursePublishMapper.selectById(courseId);
    }

//    //从缓存中查询课程
//    public CoursePublish getCoursePublishCache(Long courseId) {
//        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
//
//        if (!StringUtils.isEmpty(jsonString)){
//            //将json转成对象
//            System.out.println("=====================缓存========================");
//            return JSON.parseObject(jsonString, CoursePublish.class);
//        }else {
//            CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
//            //将数据库查询到的数据存入缓存
//            redisTemplate.opsForValue().set("course:" + courseId,
//                    JSON.toJSONString(coursePublish), 300+new Random().nextInt(100), TimeUnit.SECONDS);
//            System.out.println("-------------------------数据库----------------------------------");
//
//
//            return coursePublish;
//        }
//    }


//    //解决缓存击穿，使用同步锁（本地锁）
//    public CoursePublish getCoursePublishCache(Long courseId) {
//
//        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
//
//        if (!StringUtils.isEmpty(jsonString)){
//            //将json转成对象
//            return JSON.parseObject(jsonString, CoursePublish.class);
//        }else {
//            synchronized (this){
//                //再从缓存中查一次，双检缓存
//                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
//
//                if (!StringUtils.isEmpty(jsonString)){
//                    //将json转成对象
//                    return JSON.parseObject(jsonString, CoursePublish.class);
//                }
//
//                CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
//
//                //将数据库查询到的数据存入缓存
//                redisTemplate.opsForValue().set("course:" + courseId,
//                        JSON.toJSONString(coursePublish), 300+new Random().nextInt(100), TimeUnit.SECONDS);
//
//                return coursePublish;
//            }
//        }
//    }

    //解决缓存击穿，使用同步锁（分布式锁）
    public CoursePublish getCoursePublishCache(Long courseId) {

        String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);

        if (!StringUtils.isEmpty(jsonString)){
            //将json转成对象
            return JSON.parseObject(jsonString, CoursePublish.class);
        }else {
            //使用setnx设置一个key，谁设置成功谁拿到锁,过期时间为300s
 //           Boolean lock001 = redisTemplate.opsForValue()
 //                   .setIfAbsent("lock001", "001",300,TimeUnit.SECONDS);
            //使用redisson来获取锁
            RLock lock = redissonClient.getLock("courseQuery:" + courseId);
            lock.lock();
            try {
                //获取到锁的人去执行代码

                //再从缓存中查一次，双检缓存
                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);

                if (!StringUtils.isEmpty(jsonString)){
                    //将json转成对象
                    return JSON.parseObject(jsonString, CoursePublish.class);
                }

                CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

                //将数据库查询到的数据存入缓存
                redisTemplate.opsForValue().set("course:" + courseId,
                        JSON.toJSONString(coursePublish), 300+new Random().nextInt(100), TimeUnit.SECONDS);

                return coursePublish;
            }finally {
                lock.unlock();
            }
        }
    }

    private void saveCoursePublish(Long courseId) {
        //课程发布信息来源于预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);


    }


    private void saveCoursePublishMessage(Long courseId) {

        MqMessage mqMessage = mqMessageService.addMessage(
                "course_publish",
                String.valueOf(courseId),
                null,
                null);
        if (mqMessage == null) {
            XueChengPlusException.cast("添加消息记录失败！！！");
        }
    }
}
