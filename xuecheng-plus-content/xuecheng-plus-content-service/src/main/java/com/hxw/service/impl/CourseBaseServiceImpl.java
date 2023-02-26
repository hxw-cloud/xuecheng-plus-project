package com.hxw.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.model.PageParams;
import com.hxw.base.model.PageResult;
import com.hxw.mapper.CourseBaseMapper;
import com.hxw.mapper.CourseCategoryMapper;
import com.hxw.mapper.CourseMarketMapper;
import com.hxw.model.dto.AddCourseDto;
import com.hxw.model.dto.CourseBaseInfoDto;
import com.hxw.model.dto.EditCourseDto;
import com.hxw.model.dto.QueryCourseParamsDto;
import com.hxw.model.po.CourseBase;
import com.hxw.model.po.CourseCategory;
import com.hxw.model.po.CourseMarket;
import com.hxw.service.CourseBaseService;
import com.hxw.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends
        ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;


    @Autowired
    private CourseMarketService courseMarketService;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams,
                                                      QueryCourseParamsDto queryCourseParamsDto) {

        //根据机构id拼接查询条件

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(CourseBase::getCompanyId, companyId);
        queryWrapper.like(!StringUtils.isEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName, queryCourseParamsDto.getCourseName());


        queryWrapper.eq(!StringUtils.isEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());

        queryWrapper.eq(!StringUtils.isEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> basePage = courseBaseMapper.selectPage(page, queryWrapper);

        PageResult<CourseBase> pageResult = new PageResult<>(
                basePage.getRecords(), basePage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
        return pageResult;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //对数据封装，调用mapper实现数据持久化
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);


        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");


        int insert = courseBaseMapper.insert(courseBase);
        Long id = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(id);
        int i = this.saveCourseMarket(courseMarket);
        if (insert <= 0 || i <= 0) {
            throw new XueChengPlusException("添加课程失败");
        }
        //住装要返回的结果

        return getCourseBaseInfo(id);
    }


    public CourseBaseInfoDto getCourseBaseInfo(Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        CourseBaseInfoDto dto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, dto);
        if (courseMarket != null)
            BeanUtils.copyProperties(courseMarket, dto);
        CourseCategory mt = courseCategoryMapper.selectById(courseBase.getMt());
        CourseCategory st = courseCategoryMapper.selectById(courseBase.getSt());
        if (mt != null)
            dto.setMtName(mt.getName());
        if (st != null)
            dto.setStName(st.getName());
        return dto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long id = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null)
            XueChengPlusException.cast("课程不存在！");
        if (!courseBase.getCompanyId().equals(companyId))
            XueChengPlusException.cast("只能修改本机构的课程");
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());


        courseBaseMapper.updateById(courseBase);

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        saveCourseMarket(courseMarket);

        return this.getCourseBaseInfo(id);
    }


    private int saveCourseMarket(CourseMarket courseMarket) {

        if (StringUtils.isBlank(courseMarket.getCharge()))
            XueChengPlusException.cast("收费规则没有选择");
        if (courseMarket.getCharge().equals("201001"))
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0)
//                throw new RuntimeException("收费但价格为空");
                XueChengPlusException.cast("收费但价格为空或价格小于0");

        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        return b ? 1 : 0;
    }


}
