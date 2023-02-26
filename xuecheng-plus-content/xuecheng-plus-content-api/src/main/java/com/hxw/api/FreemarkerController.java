package com.hxw.api;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FreemarkerController {


    @GetMapping("testFreemarker")
    public ModelAndView test() {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("name", "dxx");

        modelAndView.setViewName("test");


        return modelAndView;
    }

}
