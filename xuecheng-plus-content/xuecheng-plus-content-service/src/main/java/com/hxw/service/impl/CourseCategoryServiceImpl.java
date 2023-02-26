package com.hxw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxw.mapper.CourseCategoryMapper;
import com.hxw.model.dto.CourseCategoryTreeDto;
import com.hxw.model.po.CourseCategory;
import com.hxw.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {


    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> treeDtoList = courseCategoryMapper.selectTreeNodes(id);


        ArrayList<CourseCategoryTreeDto> list = new ArrayList<>();
        HashMap<String, CourseCategoryTreeDto> hashMap = new HashMap<>();

        treeDtoList.stream().forEach(item -> {
            hashMap.put(item.getId(), item);
            if (item.getParentid().equals(id)) {
                list.add(item);
            }

            String parentid = item.getParentid();
            CourseCategoryTreeDto categoryTreeDto = hashMap.get(parentid);

            if (categoryTreeDto != null) {
                List childrenTreeNodes = categoryTreeDto.getChildrenTreeNodes();
                if (childrenTreeNodes == null) {
                    categoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                categoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });


        return list;
    }
}
