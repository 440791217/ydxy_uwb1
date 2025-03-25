package org.ydxy.uwb.app;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ydxy.uwb.entity.UwbEntity;
import org.ydxy.uwb.tool.ExpiringFixedSizeQueue;
import org.ydxy.uwb.tool.Filtering;
import org.ydxy.uwb.tool.UwbComb;
import org.ydxy.uwb.utils.UwbToa2D;
import org.ydxy.uwb.utils.UwbToa3D;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class ToaApp {

    @Getter
    @Setter
    static HashMap<SiteQueueKey, LinkedList<ExpiringFixedSizeQueue<Double>>> siteQueueMap;
    @Getter
    @Setter
    static int siteQueueSize=10;
    public static void uwbToaTF3D(UwbEntity[] entities, double results[],String tagNum, String mainDevId){
        SiteQueueKey siteQueueKey=SiteQueueKey.builder().mainDevId(mainDevId).tagNum(tagNum).build();
        if(!siteQueueMap.containsKey(siteQueueKey)){
            ExpiringFixedSizeQueue<Double> xq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            ExpiringFixedSizeQueue<Double> yq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            ExpiringFixedSizeQueue<Double> zq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            LinkedList<ExpiringFixedSizeQueue<Double>> list= new LinkedList<>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(siteQueueKey,list);
        }
        UwbToa3D.uwbToaTF3D(entities,results);
        double x=results[0],y=results[1],z=results[2];
        LinkedList<ExpiringFixedSizeQueue<Double>> list=siteQueueMap.get(siteQueueKey);
        ExpiringFixedSizeQueue<Double> xq=list.get(0);
        ExpiringFixedSizeQueue<Double> yq=list.get(1);
        ExpiringFixedSizeQueue<Double> zq=list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        log.info("滤波队列大小{}", xq.getSize());
        x=xq.medianFilter();
        y=yq.medianFilter();
        z=zq.medianFilter();
        results[0]=x;
        results[1]=y;
        results[2]=z;
    }

    public static void uwbToaTF2D(UwbEntity[] entities, double results[],String tagNum, String mainDevId){
        SiteQueueKey siteQueueKey=SiteQueueKey.builder().mainDevId(mainDevId).tagNum(tagNum).build();
        if(!siteQueueMap.containsKey(siteQueueKey)){
            ExpiringFixedSizeQueue<Double> xq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            ExpiringFixedSizeQueue<Double> yq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            ExpiringFixedSizeQueue<Double> zq=new ExpiringFixedSizeQueue<Double>(siteQueueSize);
            LinkedList<ExpiringFixedSizeQueue<Double>> list=new LinkedList<ExpiringFixedSizeQueue<Double>>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(siteQueueKey,list);
        }
        //
        UwbComb combinations = new UwbComb();
        int n = entities.length, k = 3; // 比如从1到4中选2个数字的所有组合方式
        List<List<Integer>> combs = combinations.combine(n, k);
        LinkedList<List<Double>> resultList=new LinkedList<>();
        for(List<Integer> com:combs){
            UwbEntity[] myEntities=new UwbEntity[3];
//            System.out.println(myEntities.length);
            int a,b,c;
            a=com.get(0)-1;
            b=com.get(1)-1;
            c=com.get(2)-1;
//            System.out.println(a);
//            System.out.println(b);
//            System.out.println(c);
//            System.out.println(entities.length);
            myEntities[0]=entities[a];
            myEntities[1]=entities[b];
            myEntities[2]=entities[c];
            UwbToa2D.uwbToaTF2D(myEntities,results);
            List<Double> tl=new LinkedList<>();
            tl.add(results[0]);
            tl.add(results[1]);
            tl.add(results[2]);
            resultList.add(tl);
        }
        List<Double> jjs=new LinkedList<>();
        for(List<Double> tl:resultList){
            double x2=tl.get(0);
            double y2=tl.get(1);
            double jj=0;
            for(UwbEntity entity:entities){
                double x1=entity.getP()[0];
                double y1=entity.getP()[1];
                double x0=x2-x1,y0=y2-y1;
                double dist=x0*x0+y0*y0;
                dist=Math.sqrt(dist);
                jj+=dist;
            }
            jjs.add(jj);
        }
        // 求 jjs 中最小值所在的标签值
        double minValue = jjs.get(0);
        int minIndex = 0;
        for (int i = 1; i < jjs.size(); i++) {
            double value = jjs.get(i);
            if (value < minValue) {
                minValue = value;
                minIndex = i;
            }
        }
        double x=resultList.get(minIndex).get(0),y=resultList.get(minIndex).get(1),z=1.8;
        //
//        if(0){
//            UwbToa2D.uwbToaTF2D(entities,results);
//            double x=results[0],y=results[1],z=1.8;
//        }
        LinkedList<ExpiringFixedSizeQueue<Double>> list=siteQueueMap.get(siteQueueKey);
        ExpiringFixedSizeQueue<Double> xq=list.get(0);
        ExpiringFixedSizeQueue<Double> yq=list.get(1);
        ExpiringFixedSizeQueue<Double> zq=list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        log.info("滤波队列大小:{}", xq.getSize());
        x=xq.medianFilter();
        y=yq.medianFilter();
        z=zq.medianFilter();
        results[0]=x;
        results[1]=y;
        results[2]=z;
    }


    public static JSONArray uwbToaTF2DofList(List<UwbEntity> distances){
        JSONArray ja=new JSONArray();
//        ExpiringFixedSizeQueue<Double> xq1=new ExpiringFixedSizeQueue<>(10);
//        ExpiringFixedSizeQueue<Double> yq1=new ExpiringFixedSizeQueue<>(10);
//        ExpiringFixedSizeQueue<Double> xq2=new ExpiringFixedSizeQueue<>(20);
//        ExpiringFixedSizeQueue<Double> yq2=new ExpiringFixedSizeQueue<>(20);
        ArrayList<Double> xl=new ArrayList<>();
        ArrayList<Double> yl=new ArrayList<>();
        int tsDif=50;
        distances.sort(Comparator.comparingLong(UwbEntity::getTs));
        HashMap<String,UwbEntity> gwMaps=new HashMap<>();
//        double x1=0,y1=0;
//        double x2=0,y2=0;
        for (UwbEntity t : distances) {
//            x1=x2;
//            y1=y2;
            gwMaps.put(t.getDevId(), t);
            List<UwbEntity> valueList = new ArrayList<>(gwMaps.values());
//            System.out.println("ls:"+valueList.size());
            long tsNow = t.getTs();
            long tsBegin=tsNow-tsDif;
            // 使用流操作过滤
            valueList = valueList.stream()
                    .filter(entity -> entity.getTs() >= tsBegin)
                    .collect(Collectors.toList());
            double[] results={0,0,0};
            findBest2d(valueList,results);
            if(results[0]==0&&results[1]==0&results[2]==0){
                continue;
            }
            xl.add(results[0]);
            yl.add(results[1]);
//            xq1.add(results[0]);
//            yq1.add(results[1]);
//            x2=xq1.medianFilter();
//            y2=yq1.medianFilter();
//            xq2.add(x2);
//            yq2.add(y2);
//            JSONObject object=new JSONObject();
//            object.put("x",xq2.meanFilter());
//            object.put("y",yq2.meanFilter());
//            object.put("z",1.8*100);
//            object.put("ts",tsNow);
//            ja.add(object);
        }
        xl=Filtering.medianFilter(xl,11);
        yl=Filtering.medianFilter(yl,11);
        xl=Filtering.meanFilter(xl,30);
        yl=Filtering.meanFilter(yl,30);
        for(int i=0;i<xl.size();i++){
            JSONObject object=new JSONObject();
            object.put("x",xl.get(i));
            object.put("y",yl.get(i));
//            object.put("z",1.8*100);
//            object.put("ts",tsNow);
            ja.add(object);
        }
        return ja;
    }

    public static void findBest2d(List<UwbEntity> entities, double[] results){
        if (entities.size() < 3) {
            return;
        }
        UwbComb combinations = new UwbComb();
        int n = entities.size();
        List<List<Integer>> combs = combinations.combine(n, 3);
        LinkedList<List<Double>> resultList=new LinkedList<>();
        for(List<Integer> com:combs){
            UwbEntity[] myEntities=new UwbEntity[3];
            int a,b,c;
            UwbEntity entity;
            a=com.get(0)-1;
            b=com.get(1)-1;
            c=com.get(2)-1;

            myEntities[0]=entities.get(a);
            myEntities[1]=entities.get(b);
            myEntities[2]=entities.get(c);
            UwbToa2D.uwbToaTF2D(myEntities,results);
            List<Double> tl=new LinkedList<>();
            tl.add(results[0]);
            tl.add(results[1]);
            tl.add(results[2]);
            resultList.add(tl);
        }
        LinkedList<Double> jjs=new LinkedList<>();
        for(List<Double> tl:resultList){
            double x2=tl.get(0);
            double y2=tl.get(1);
            double jj=0;
            for(UwbEntity entity:entities){
                double x1=entity.getP()[0];
                double y1=entity.getP()[1];
                double x0=x2-x1,y0=y2-y1;
                double dist=x0*x0+y0*y0;
                dist=Math.sqrt(dist);
                jj+=dist;
            }
            jjs.add(jj);
        }
        // 求 jjs 中最小值所在的标签值
        double minValue = jjs.get(0);
        int minIndex = 0;
        for (int i = 1; i < jjs.size(); i++) {
            double value = jjs.get(i);
            if (value < minValue) {
                minValue = value;
                minIndex = i;
            }
        }
        double x=resultList.get(minIndex).get(0),y=resultList.get(minIndex).get(1),z=1.8;
        results[0]=x;
        results[1]=y;
        results[2]=z;
//        System.out.println(results);
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
        uwbToaTF2D(uwbEntityList.toArray(new UwbEntity[4]), results,"1", "000000");
        System.out.println(Arrays.toString(results));

        JSONObject object=new JSONObject();
        object.put("entities",uwbEntityList.toArray(new UwbEntity[4]));
        System.out.println(object.toString());
    }

    @Data
    @Builder
    static class SiteQueueKey{
        private String mainDevId;
        private String tagNum;
    }

}
