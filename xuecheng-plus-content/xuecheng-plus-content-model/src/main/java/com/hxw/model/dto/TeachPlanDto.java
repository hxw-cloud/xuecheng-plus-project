package com.hxw.model.dto;

import com.hxw.model.po.TeachPlan;
import com.hxw.model.po.TeachPlanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachPlanDto extends TeachPlan {

    //课程计划关联的媒资信息
    TeachPlanMedia teachplanMedia;

    //子目录
    List<TeachPlanDto> teachPlanTreeNodes;
}