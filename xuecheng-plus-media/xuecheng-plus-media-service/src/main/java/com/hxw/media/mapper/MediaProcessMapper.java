package com.hxw.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hxw.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {


    @Select("select t.* from media_process" +
            " t where t.id % #{shardTotal} = #{shardIndex} " +
            "limit #{shardCount}")
    public List<MediaProcess> selectListByShardIndex(
            @Param("shardTotal") int shardTotal,
            @Param("shardIndex") int shardIndex,
            @Param("shardCount") int shardCount
    );

}
