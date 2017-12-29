package com.github.conanchen.gedit.hello.controller;

import com.google.gson.Gson;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.util.Date;

@RestController
@EnableAutoConfiguration
public class HelloController {
    private final static Gson gson = new Gson();
//
//    @Autowired
//    private WordRepository wordRepository;

    @RequestMapping(value = "/hello")
    public String hello() {
        return "hello@" + DateFormat.getInstance().format(new Date()) + ", HelloController Spring Boot ";
    }
}