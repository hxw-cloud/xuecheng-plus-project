package com.hxw.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.mapper.TeachplanMapper;
import com.hxw.mapper.TeachplanMediaMapper;
import com.hxw.model.dto.BindTeachplanMediaDto;
import com.hxw.model.dto.SaveTeachPlanDto;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.model.po.TeachPlan;
import com.hxw.model.po.TeachPlanMedia;
import com.hxw.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, TeachPlan> implements TeachplanService {


    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> selectTreeNodes(Long id) {
        return teachplanMapper.selectTreeNodes(id);
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        Long id = saveTeachPlanDto.getId();
        TeachPlan teachPlan = teachplanMapper.selectById(id);
        if (teachPlan == null) {
            teachPlan = new TeachPlan();
            BeanUtils.copyProperties(saveTeachPlanDto, teachPlan);
            int count = getTeachPlanCount(saveTeachPlanDto.getCourseId(),
                    saveTeachPlanDto.getParentid());

            teachPlan.setOrderby(count + 1);

            teachplanMapper.insert(teachPlan);
        } else {
            BeanUtils.copyProperties(saveTeachPlanDto, teachPlan);
            teachplanMapper.updateById(teachPlan);
        }
    }

    @Transactional
    @Override
    public TeachPlanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        Long teachableId = bindTeachplanMediaDto.getTeachplanId();

        TeachPlan teachPlan = teachplanMapper.selectById(teachableId);
        if (teachPlan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachPlan.getGrade();
        if (grade != 2) {
            XueChengPlusException.cast("自有二级目录可以绑定");
        }
        LambdaQueryWrapper<TeachPlanMedia> wrapper = new LambdaQueryWrapper<TeachPlanMedia>()
                .eq(TeachPlanMedia::getTeachplanId, teachableId)
                .eq(TeachPlanMedia::getMediaId, bindTeachplanMediaDto.getMediaId());
        teachplanMediaMapper.delete(wrapper);

        TeachPlanMedia teachPlanMedia = new TeachPlanMedia();
        teachPlanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachPlanMedia.setCourseId(teachPlan.getCourseId());
        teachPlanMedia.setTeachplanId(teachableId);
        teachPlanMedia.setCreateDate(LocalDateTime.now());
        teachPlanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachPlanMedia);
        return teachPlanMedia;
    }

    @Override
    public void delAassociationMedia(Long teachPlanId, String mediaId) {
        TeachPlan teachPlan = teachplanMapper.selectById(teachPlanId);
        if (teachPlan == null) {
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachPlan.getGrade();
        if (grade != 2) {
            XueChengPlusException.cast("自有二级目录可以删除");
        }
        LambdaQueryWrapper<TeachPlanMedia> wrapper = new LambdaQueryWrapper<TeachPlanMedia>()
                .eq(TeachPlanMedia::getTeachplanId, teachPlanId)
                .eq(TeachPlanMedia::getMediaId, mediaId);
        teachplanMediaMapper.delete(wrapper);
    }


    public int getTeachPlanCount(Long countId, Long parentId) {
        LambdaQueryWrapper<TeachPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachPlan::getCourseId, countId);
        wrapper.eq(TeachPlan::getParentid, parentId);
        Integer integer = teachplanMapper.selectCount(wrapper);
        return integer.intValue();
    }
}
