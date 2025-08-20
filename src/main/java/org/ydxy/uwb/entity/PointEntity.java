package org.ydxy.uwb.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointEntity {
    private double x;
    private double y;
    private String tagId;
    private long ts;
    private String devId;
}
