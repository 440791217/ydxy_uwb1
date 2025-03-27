package org.ydxy.uwb.app;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ydxy.uwb.entity.PointEntity;
import org.ydxy.uwb.entity.UwbEntity;
import org.ydxy.uwb.tool.*;
import org.ydxy.uwb.utils.UwbToa2D;
import org.ydxy.uwb.utils.UwbToa3D;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Data
public class ToaApp {

    @Getter
    @Setter
    static HashMap<SiteQueueKey, LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>>> siteQueueMap;
    @Getter
    @Setter
    static int siteQueueSize = 10;

    public static void uwbToaTF3D(UwbEntity[] entities, double results[], String tagNum, String mainDevId) {
        SiteQueueKey siteQueueKey = SiteQueueKey.builder().mainDevId(mainDevId).tagNum(tagNum).build();
        if (!siteQueueMap.containsKey(siteQueueKey)) {
            ExpiringFixedSizeNumberAnalysisQueue<Double> xq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            ExpiringFixedSizeNumberAnalysisQueue<Double> yq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            ExpiringFixedSizeNumberAnalysisQueue<Double> zq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>> list = new LinkedList<>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(siteQueueKey, list);
        }
        UwbToa3D.uwbToaTF3D(entities, results);
        double x = results[0], y = results[1], z = results[2];
        LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>> list = siteQueueMap.get(siteQueueKey);
        ExpiringFixedSizeNumberAnalysisQueue<Double> xq = list.get(0);
        ExpiringFixedSizeNumberAnalysisQueue<Double> yq = list.get(1);
        ExpiringFixedSizeNumberAnalysisQueue<Double> zq = list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        log.info("滤波队列大小{}", xq.getSize());
        x = xq.medianFilter();
        y = yq.medianFilter();
        z = zq.medianFilter();
        results[0] = x;
        results[1] = y;
        results[2] = z;
    }

    public static void uwbToaTF2D(UwbEntity[] entities, double results[], String tagNum, String mainDevId) {
        SiteQueueKey siteQueueKey = SiteQueueKey.builder().mainDevId(mainDevId).tagNum(tagNum).build();
        if (!siteQueueMap.containsKey(siteQueueKey)) {
            ExpiringFixedSizeNumberAnalysisQueue<Double> xq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            ExpiringFixedSizeNumberAnalysisQueue<Double> yq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            ExpiringFixedSizeNumberAnalysisQueue<Double> zq = new ExpiringFixedSizeNumberAnalysisQueue<Double>(siteQueueSize);
            LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>> list = new LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>>();
            list.add(xq);
            list.add(yq);
            list.add(zq);
            siteQueueMap.put(siteQueueKey, list);
        }
        //
        UwbComb combinations = new UwbComb();
        int n = entities.length, k = 3; // 比如从1到4中选2个数字的所有组合方式
        List<List<Integer>> combs = combinations.combine(n, k);
        LinkedList<List<Double>> resultList = new LinkedList<>();
        for (List<Integer> com : combs) {
            UwbEntity[] myEntities = new UwbEntity[3];
//            System.out.println(myEntities.length);
            int a, b, c;
            a = com.get(0) - 1;
            b = com.get(1) - 1;
            c = com.get(2) - 1;
//            System.out.println(a);
//            System.out.println(b);
//            System.out.println(c);
//            System.out.println(entities.length);
            myEntities[0] = entities[a];
            myEntities[1] = entities[b];
            myEntities[2] = entities[c];
            UwbToa2D.uwbToaTF2D(myEntities, results);
            List<Double> tl = new LinkedList<>();
            tl.add(results[0]);
            tl.add(results[1]);
            tl.add(results[2]);
            resultList.add(tl);
        }
        List<Double> jjs = new LinkedList<>();
        for (List<Double> tl : resultList) {
            double x2 = tl.get(0);
            double y2 = tl.get(1);
            double jj = 0;
            for (UwbEntity entity : entities) {
                double x1 = entity.getP()[0];
                double y1 = entity.getP()[1];
                double x0 = x2 - x1, y0 = y2 - y1;
                double dist = x0 * x0 + y0 * y0;
                dist = Math.sqrt(dist);
                jj += dist;
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
        double x = resultList.get(minIndex).get(0), y = resultList.get(minIndex).get(1), z = 1.8;
        //
//        if(0){
//            UwbToa2D.uwbToaTF2D(entities,results);
//            double x=results[0],y=results[1],z=1.8;
//        }
        LinkedList<ExpiringFixedSizeNumberAnalysisQueue<Double>> list = siteQueueMap.get(siteQueueKey);
        ExpiringFixedSizeNumberAnalysisQueue<Double> xq = list.get(0);
        ExpiringFixedSizeNumberAnalysisQueue<Double> yq = list.get(1);
        ExpiringFixedSizeNumberAnalysisQueue<Double> zq = list.get(2);
        xq.add(x);
        yq.add(y);
        zq.add(z);
        log.info("滤波队列大小:{}", xq.getSize());
        x = xq.medianFilter();
        y = yq.medianFilter();
        z = zq.medianFilter();
        results[0] = x;
        results[1] = y;
        results[2] = z;
    }

    static Map<SiteQueueKey, UwbEntity> KEY_TO_UWB_ENTITY_MAP = new ConcurrentHashMap<>();
    static Map<String, ExpiringFixedSizeQueue<PointEntity>> TAG_ID_TO_POINT_QUEUE_FOR_MEDIAN_MAP = new ConcurrentHashMap<>();
    static Map<String, ExpiringFixedSizeQueue<PointEntity>> TAG_ID_TO_POINT_QUEUE_FOR_MEAN_MAP = new ConcurrentHashMap<>();

    public static List<PointEntity> uwbToaTF2DofList(List<UwbEntity> uwbEntityList) {
        // 窗口大小
        int MEDIAN_WINDOW_SIZE = 1000;
        long MEDIAN_WINDOW_MILLS_SIZE = 4000L;
        int MEAN_WINDOW_SIZE = 1000;
        long MEAN_WINDOW_MILLS_SIZE = 4000L;
        // 排序
        uwbEntityList.sort(Comparator.comparingLong(UwbEntity::getTs));
        List<PointEntity> resList = new ArrayList<>();
        for (UwbEntity uwbEntity : uwbEntityList) {
            String tagId = uwbEntity.getTagId();
            String devId = uwbEntity.getDevId();
            long ts = uwbEntity.getTs();
            SiteQueueKey key = SiteQueueKey.builder().mainDevId(devId).tagNum(tagId).build();
            // 缓存uwbEntity
            KEY_TO_UWB_ENTITY_MAP.put(key, uwbEntity);
            // 从缓存中取出可用于计算的uwbEntity列表
            List<UwbEntity> uwbEntityForCalList = getUwbEntityForCalList(uwbEntity);
            // 计算坐标
            double[] results = {0, 0, 0};
            findBest2d(uwbEntityForCalList, results);
            double x = results[0];
            double y = results[1];
            double z = results[2];
            if (x == 0 && y == 0 & z == 0) {
                continue;
            }
            PointEntity pointEntity = PointEntity.builder().x(x).y(y).tagId(tagId).build();
            // 中值滤波
            TAG_ID_TO_POINT_QUEUE_FOR_MEDIAN_MAP.putIfAbsent(tagId, new ExpiringFixedSizeQueue<>(MEDIAN_WINDOW_SIZE, MEDIAN_WINDOW_MILLS_SIZE));
            ExpiringFixedSizeQueue<PointEntity> queueForMedian = TAG_ID_TO_POINT_QUEUE_FOR_MEDIAN_MAP.get(tagId);
            queueForMedian.add(pointEntity, ts);
            if (!queueForMedian.isFull()) {
                // 不够中值窗口，先不算
                continue;
            }
            x = Filtering.getMedian(queueForMedian.toList().stream().map(PointEntity::getX).collect(Collectors.toList()));
            y = Filtering.getMedian(queueForMedian.toList().stream().map(PointEntity::getY).collect(Collectors.toList()));
            pointEntity = PointEntity.builder().x(x).y(y).tagId(tagId).build();
            // 均值滤波
            TAG_ID_TO_POINT_QUEUE_FOR_MEAN_MAP.putIfAbsent(tagId, new ExpiringFixedSizeQueue<>(MEAN_WINDOW_SIZE, MEAN_WINDOW_MILLS_SIZE));
            ExpiringFixedSizeQueue<PointEntity> queueForMean = TAG_ID_TO_POINT_QUEUE_FOR_MEAN_MAP.get(tagId);
            queueForMean.add(pointEntity, ts);
            if (!queueForMean.isFull()) {
                // 不够均值窗口，先不算
                continue;
            }
            x = Filtering.getMean(queueForMean.toList().stream().map(PointEntity::getX).collect(Collectors.toList()));
            y = Filtering.getMean(queueForMean.toList().stream().map(PointEntity::getY).collect(Collectors.toList()));
            pointEntity = PointEntity.builder().x(x).y(y).tagId(tagId).build();
            // 返回
            resList.add(pointEntity);
        }
        return resList;
    }

    private static List<UwbEntity> getUwbEntityForCalList(UwbEntity uwbEntity) {
        int tsDif = 50;
        List<UwbEntity> uwbEntityForCalList = new ArrayList<>();
        long tsNow = uwbEntity.getTs();
        long tsBegin = tsNow - tsDif;
        for (Map.Entry<SiteQueueKey, UwbEntity> uwbEntityEntry : KEY_TO_UWB_ENTITY_MAP.entrySet()) {
            SiteQueueKey entryKey = uwbEntityEntry.getKey();
            UwbEntity entryValue = uwbEntityEntry.getValue();
            if (entryKey.getTagNum().equals(uwbEntity.getTagId()) && entryValue.getTs() >= tsBegin) {
                uwbEntityForCalList.add(entryValue);
            }
        }
        return uwbEntityForCalList;
    }

    public static void findBest2d(List<UwbEntity> entities, double[] results) {
        if (entities.size() < 3) {
            return;
        }
        UwbComb combinations = new UwbComb();
        int n = entities.size();
        List<List<Integer>> combs = combinations.combine(n, 3);
        LinkedList<List<Double>> resultList = new LinkedList<>();
        for (List<Integer> com : combs) {
            UwbEntity[] myEntities = new UwbEntity[3];
            int a, b, c;
            UwbEntity entity;
            a = com.get(0) - 1;
            b = com.get(1) - 1;
            c = com.get(2) - 1;

            myEntities[0] = entities.get(a);
            myEntities[1] = entities.get(b);
            myEntities[2] = entities.get(c);
            UwbToa2D.uwbToaTF2D(myEntities, results);
            List<Double> tl = new LinkedList<>();
            tl.add(results[0]);
            tl.add(results[1]);
            tl.add(results[2]);
            resultList.add(tl);
        }
        LinkedList<Double> jjs = new LinkedList<>();
        for (List<Double> tl : resultList) {
            double x2 = tl.get(0);
            double y2 = tl.get(1);
            double jj = 0;
            for (UwbEntity entity : entities) {
                double x1 = entity.getP()[0];
                double y1 = entity.getP()[1];
                double x0 = x2 - x1, y0 = y2 - y1;
                double dist = x0 * x0 + y0 * y0;
                dist = Math.sqrt(dist);
                jj += dist;
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
        double x = resultList.get(minIndex).get(0), y = resultList.get(minIndex).get(1), z = 1.8;
        results[0] = x;
        results[1] = y;
        results[2] = z;
//        System.out.println(results);
    }

    public static void init() {
        siteQueueMap = new HashMap<>();
    }


    public static void main(String[] args) {
        System.out.println("ToaApp launch!");
        init();
        List<UwbEntity> uwbEntityList = new ArrayList<>();
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
        uwbToaTF2D(uwbEntityList.toArray(new UwbEntity[4]), results, "1", "000000");
        System.out.println(Arrays.toString(results));

        JSONObject object = new JSONObject();
        object.put("entities", uwbEntityList.toArray(new UwbEntity[4]));
        System.out.println(object.toString());
    }

    @Data
    @Builder
    static class SiteQueueKey {
        private String mainDevId;
        private String tagNum;
    }

}
