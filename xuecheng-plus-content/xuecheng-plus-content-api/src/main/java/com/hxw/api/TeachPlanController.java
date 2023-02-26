package com.hxw.api;

import com.hxw.model.dto.BindTeachplanMediaDto;
import com.hxw.model.dto.SaveTeachPlanDto;
import com.hxw.model.dto.TeachPlanDto;
import com.hxw.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")

@RestController
public class TeachPlanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",
            name = "课程Id",
            required = true,
            dataType = "Long",
            paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable("courseId") Long courseId) {
        return teachplanService.selectTreeNodes(courseId);
    }


    @PostMapping("teachplan")
    public void saveTeachPlan(@RequestBody SaveTeachPlanDto saveTeachPlanDto) {
        teachplanService.saveTeachPlan(saveTeachPlanDto);
    }


    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {

        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation(value = "课程计划和媒资信息解除绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void delAssociationMedia(@PathVariable Long teachPlanId,
                                    @PathVariable String mediaId) {

        teachplanService.delAassociationMedia(teachPlanId, mediaId);
    }


}