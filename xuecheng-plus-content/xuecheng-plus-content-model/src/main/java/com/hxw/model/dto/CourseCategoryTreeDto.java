package com.hxw.model.dto;

import com.hxw.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

@Data
public class CourseCategoryTreeDto extends CourseCategory {

    List childrenTreeNodes;
}
