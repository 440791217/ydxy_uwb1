package org.ydxy.uwb.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;

@Data
public class UwbEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public String devId;
	public String tagId;
	public long ts;
	/**
	 * 坐标，依次x,y,z
	 */
	public double[] p;

	/**
	 * 距离值
	 */
	public double dist;

}
