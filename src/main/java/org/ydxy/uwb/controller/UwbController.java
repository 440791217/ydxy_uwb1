package org.ydxy.uwb.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ydxy.uwb.app.ToaApp;
import org.ydxy.uwb.entity.UwbEntity;
import org.ydxy.uwb.http.HttpResponse;

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
        if(entities.length>3){
            ToaApp.uwbToaTF3D(entities,results,tagNum);
        }else if(entities.length==3){
            ToaApp.uwbToaTF2D(entities,results,tagNum);
        }else{
            results=null;
        }
        JSONObject resultsJson=new JSONObject();
        JSONObject object=new JSONObject();
        int rc=0;
        if(results!=null){
            resultsJson.put("x",results[0]);
            resultsJson.put("y",results[1]);
            resultsJson.put("z",results[2]);
            object.put("results",resultsJson);
            object.put("tagNum",tagNum);
        }else{
            rc=1;
        }
        object.put("rc",rc);


        return  HttpResponse.getResponse(object).toString();
    }
}
