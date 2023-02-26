package com.hxw.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hxw.system.model.po.Dictionary;

import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author itcast
 * @since 2021-10-07
 */
public interface DictionaryService extends IService<Dictionary> {

    /**
     * 查询所有数据字典内容
     */
    List<Dictionary> queryAll();

    /**
     * 根据code查询数据字典
     *
     * @param code -- String 数据字典Code
     */
    Dictionary getByCode(String code);
}
