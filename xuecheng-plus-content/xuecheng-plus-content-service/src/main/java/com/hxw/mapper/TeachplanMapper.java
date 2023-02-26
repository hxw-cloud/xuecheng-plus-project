package com.hxw.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.model.po.TeachPlan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<TeachPlan> {


    public List<TeachPlanDto> selectTreeNodes(Long id);
}
