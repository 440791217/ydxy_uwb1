package org.ydxy.uwb.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ydxy.uwb.app.ToaApp;
import org.ydxy.uwb.entity.PointEntity;
import org.ydxy.uwb.entity.UwbEntity;
import org.ydxy.uwb.entity.UwbEntity1;
import org.ydxy.uwb.http.HttpResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequestMapping("/uwb")
@RestController
public class UwbController {

    @PostMapping("/toa3d")
    public String uwbToaTF3D(@RequestBody JSONObject body){
        log.info("************开始************");
        log.info("收到数据:{}", body);
        double[] results=new double[3];
        UwbEntity[] entities=body.getObject("entities",UwbEntity[].class);
        Arrays.sort(entities, Comparator.comparingDouble(entity -> entity.dist));

        String tagNum=body.getString("tagNum");
        String mainDevId= (String) body.getOrDefault("mainDevId", "000000");

        if(entities.length>3){
            ToaApp.uwbToaTF2D(entities,results,tagNum, mainDevId);
        }else if(entities.length==3){
            ToaApp.uwbToaTF2D(entities,results,tagNum, mainDevId);
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
            log.info("主基站{},卡号{},算出的坐标({},{},{})",mainDevId,tagNum, results[0],results[1],results[2]);
        }else{
            rc=1;
        }
        object.put("rc",rc);


        log.info("************完成************");
        return  HttpResponse.getResponse(object).toString();
    }

    @PostMapping("/toa3d_of_list")
    public String uwbToaTF3DofList(@RequestBody JSONObject body){
        log.info("************开始************");
        log.info("收到:{}", body);
        JSONArray ja=body.getJSONArray("distances");
        List<UwbEntity1> distances1=ja.toJavaList(UwbEntity1.class);
        ArrayList<UwbEntity> distances2=new ArrayList<>();
        for(UwbEntity1 t1:distances1){
            UwbEntity t2=new UwbEntity();
            t2.setTagId(t1.getTagID());
            t2.setDevId(t1.getTargetGatewayId());
            t2.setTs(t1.getTs());
            t2.setDist(t1.getDistance());
            double[] p=new double[3];
            p[0]=t1.getTargetGatewayX();
            p[1]=t1.getTargetGatewayY();
            p[2]=t1.getTargetGatewayZ();
            t2.setP(p);
            distances2.add(t2);
        }
        List<PointEntity> results=ToaApp.uwbToaTF2DofList(distances2);
        JSONObject rsp=new JSONObject();
        rsp.put("results",results);
        rsp.put("rc",0);
        log.info("返回:{}", rsp);
        log.info("************完成************");
        return HttpResponse.getResponse(rsp).toString();
    }

    public static void main(String[] args){


    }
}
