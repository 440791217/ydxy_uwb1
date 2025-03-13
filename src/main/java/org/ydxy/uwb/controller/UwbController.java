package org.ydxy.uwb.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ydxy.uwb.app.ToaApp;
import org.ydxy.uwb.entity.UwbEntity;

import java.util.Arrays;
import java.util.Comparator;

@RequestMapping("/uwb")
@RestController
public class UwbController {

    @PostMapping("/toa3d")
    public String uwbToaTF3D(@RequestBody JSONObject body){
        double[] results=new double[3];
        UwbEntity[] entities=body.getObject("entities",UwbEntity[].class);

        Arrays.sort(entities, Comparator.comparingDouble(entity -> entity.dist));


        String tagNum=body.getString("tagNum");
        ToaApp.uwbToaTF3D(entities,results,tagNum);
        JSONObject resultsJson=new JSONObject();
        resultsJson.put("x",results[0]);
        resultsJson.put("y",results[1]);
        resultsJson.put("z",results[2]);
        JSONObject object=new JSONObject();
        object.put("code",200);
        object.put("results",resultsJson);
        object.put("tagNum",tagNum);

        return  object.toString();
    }
}
