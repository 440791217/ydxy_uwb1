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
import org.ydxy.uwb.utils.UwbInner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequestMapping("/uwb")
@RestController
public class UwbController {

    /**
     * 3D TOA（暂时弃用）
     * @param body
     * @return
     */
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
        log.info("************toa3d_of_list开始************");
//        log.info("收到:{}", body);
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
//        log.info("返回:{}", rsp);
        log.info("************toa3d_of_list完成************");
        return HttpResponse.getResponse(rsp).toString();
    }

    @PostMapping("/toa2d_of_inner")
    public String uwbToaTF3DofInner(@RequestBody JSONObject body){
        log.info("************开始************");
        log.info("收到数据:{}", body);
        UwbEntity[] entities=body.getObject("entities",UwbEntity[].class);
        double x1=entities[0].p[0],y1=entities[0].p[1],dist1=entities[0].dist;
        double x2=entities[1].p[0],y2=entities[1].p[1],dist2=entities[1].dist;
        List<UwbInner.Point> list=UwbInner.solve(x1,y1,dist1,x2,y2,dist2);
        JSONObject rsp=new JSONObject();
        rsp.put("results",list);
        rsp.put("rc",0);
//        System.out.println(rsp.toString());
        log.info("返回:{}", rsp);
        log.info("************完成************");
//        for (Object p : rsp.getJSONArray("results")) {
//            System.out.println(p);
//        }
        return HttpResponse.getResponse(rsp).toString();
    }

    @PostMapping("/tdoa")
    public String uwbTdoa(@RequestBody String body){
        log.info("************开始************");
        log.info("收到数据:{}", body);
        JSONArray array=JSONArray.parseArray(body);
        List<TdoaApp.Input> inputs=new ArrayList<>();
        for(Object object : array){
                JSONObject obj=JSONObject.parseObject(object.toString());
                long ts=obj.getLong("ts");
                long tagId=obj.getLong("tagId");
                List<String> gatewayIdList= obj.getJSONArray("gatewayIdList").toJavaList(String.class);
                List<Double> xList = obj.getJSONArray("gatewayXList").toJavaList(Double.class);
                List<Double> yList = obj.getJSONArray("gatewayYList").toJavaList(Double.class);
                List<Double> zList = obj.getJSONArray("gatewayZList").toJavaList(Double.class);
                List<Double> timeDiff=obj.getJSONArray("timeDiff").toJavaList(Double.class);

                TdoaApp.Input input=new TdoaApp.Input();
                input.ts=ts;
                input.tagId=tagId;
                input.xList=xList;
                input.yList=yList;
                input.zList=zList;
                input.tdoaData=timeDiff;
                inputs.add(input);
        }

        TdoaApp.uwbTdoa(inputs);

        return "hello";
    }
    public static void main(String[] args){


    }
}
