package com.hxw.system;


import com.hxw.system.mapper.DictionaryMapper;
import com.hxw.system.model.po.Dictionary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class systemservicetest {


    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Test
    void content() {
        Dictionary dictionary = dictionaryMapper.selectById(12);
        System.out.println(dictionary);
    }
}
