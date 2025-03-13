package org.ydxy.uwb.app;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.ydxy.uwb.entity.UwbEntity;
import org.ydxy.uwb.tool.FixedSizeQueue;
import org.ydxy.uwb.utils.UwbToa2D;
import org.ydxy.uwb.utils.UwbToa3D;

import java.util.*;

public class ToaApp {

    @Getter
    @Setter
    static HashMap<String, LinkedList<FixedSizeQueue<Double>>> siteQueueMap;
    @Getter
    @Setter
    static int siteQueueSize=5;
    public static void uwbToaTF3D(UwbEntity[] entities, double results[],String tagNum){

        if(!siteQueueMap.containsKey(tagNum)){
            FixedSizeQueue<Double> xq=new FixedSizeQueue<Double>(siteQueueSize);
            FixedSizeQueue<Double> yq=new FixedSizeQueue<Double>(siteQueueSize);
            FixedSizeQueue<Double> zq=new FixedSizeQueue<Double>(siteQueueSize);
            LinkedList<FixedSizeQueue<Double>> list=new LinkedList<FixedSizeQueue<Double>>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(tagNum,list);
        }
        UwbToa3D.uwbToaTF3D(entities,results);
        double x=results[0],y=results[1],z=results[2];
        LinkedList<FixedSizeQueue<Double>> list=siteQueueMap.get(tagNum);
        FixedSizeQueue<Double> xq=list.get(0);
        FixedSizeQueue<Double> yq=list.get(1);
        FixedSizeQueue<Double> zq=list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        x=xq.meanFilter();
        y=yq.meanFilter();
        z=zq.meanFilter();
        results[0]=x;
        results[1]=y;
        results[2]=z;
    }

    public static void uwbToaTF2D(UwbEntity[] entities, double results[],String tagNum){

        if(!siteQueueMap.containsKey(tagNum)){
            FixedSizeQueue<Double> xq=new FixedSizeQueue<Double>(siteQueueSize);
            FixedSizeQueue<Double> yq=new FixedSizeQueue<Double>(siteQueueSize);
            FixedSizeQueue<Double> zq=new FixedSizeQueue<Double>(siteQueueSize);
            LinkedList<FixedSizeQueue<Double>> list=new LinkedList<FixedSizeQueue<Double>>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(tagNum,list);
        }
        UwbToa2D.uwbToaTF2D(entities,results);
        double x=results[0],y=results[1],z=1.8;
        LinkedList<FixedSizeQueue<Double>> list=siteQueueMap.get(tagNum);
        FixedSizeQueue<Double> xq=list.get(0);
        FixedSizeQueue<Double> yq=list.get(1);
        FixedSizeQueue<Double> zq=list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        x=xq.meanFilter();
        y=yq.meanFilter();
        z=zq.meanFilter();
        results[0]=x;
        results[1]=y;
        results[2]=z;
    }

    public static void init(){
        siteQueueMap=new HashMap<>();
    }



    public static void main(String[] args){
        System.out.println("ToaApp launch!");
        init();
        List<UwbEntity> uwbEntityList =  new ArrayList<>();
        // 基站1采集
        UwbEntity u1 = new UwbEntity();
        u1.setDevId("YDJZ-25010001");
        u1.setP(new double[]{179, 370, 182});
        u1.setDist(170);
        uwbEntityList.add(u1);
        // 基站2采集
        UwbEntity u2 = new UwbEntity();
        u2.setDevId("YDJZ-25010009");
        u2.setP(new double[]{186, 1256, 185});
        u2.setDist(1061);
        uwbEntityList.add(u2);
        // 基站3采集
        UwbEntity u3 = new UwbEntity();
        u3.setDevId("YDJZ-25010004");
        u3.setP(new double[]{948, 1280, 190});
        u3.setDist(1349);
        uwbEntityList.add(u3);
        // 基站4采集
        UwbEntity u4 = new UwbEntity();
        u4.setDevId("YDJZ-25010010");
        u4.setP(new double[]{948, 370, 195});
        u4.setDist(798);
        uwbEntityList.add(u4);
        // 计算
        double[] results = new double[3];
        uwbToaTF3D(uwbEntityList.toArray(new UwbEntity[4]), results,"1");
        System.out.println(Arrays.toString(results));

        JSONObject object=new JSONObject();
        object.put("entities",uwbEntityList.toArray(new UwbEntity[4]));
        System.out.println(object.toString());
    }

}
