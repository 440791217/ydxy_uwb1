package org.ydxy.uwb.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;

@Data
public class UwbEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public String devId;

	/**
	 * 坐标，依次x,y,z
	 */
	public double[] p;

	/**
	 * 距离值
	 */
	public double dist;

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	public double[] getP() {
		return p;
	}

	public void setP(double[] p) {
		this.p = p;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}
}
