package org.ydxy.uwb.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {

    @PostMapping("/hello")
    String hello(){
        return "hello";
    }

    @PostMapping("/helloJson")
    String helloJson(@RequestBody JSONObject body){
        return body.toString();
    }
}
