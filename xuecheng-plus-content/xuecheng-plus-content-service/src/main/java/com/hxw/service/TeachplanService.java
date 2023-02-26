package com.hxw.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hxw.model.dto.BindTeachplanMediaDto;
import com.hxw.model.dto.SaveTeachPlanDto;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.model.po.TeachPlan;
import com.hxw.model.po.TeachPlanMedia;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-01-23
 */
public interface TeachplanService extends IService<TeachPlan> {


    public List<TeachPlanDto> selectTreeNodes(Long id);


    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);


    /**
     * @param bindTeachplanMediaDto
     * @return com.hxw.content.model.po.TeachplanMedia
     * @description 教学计划板顶媒资
     */
    public TeachPlanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * @param teachPlanId 教学计划id
     * @param mediaId     媒资文件id
     * @return void
     * @description 删除教学计划与媒资之间的绑定关系
     */
    public void delAassociationMedia(Long teachPlanId, String mediaId);


}
