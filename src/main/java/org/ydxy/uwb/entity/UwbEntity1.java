package org.ydxy.uwb.entity;

import lombok.Data;

@Data
public class UwbEntity1 {
    double distance;
    long ts;
    String tagID;
    String targetGatewayId;
    double targetGatewayX;
    double targetGatewayY;
    double targetGatewayZ;
}
