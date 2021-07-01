package com.adtran.mosaicone.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ip {
	@JsonProperty("ip")
	private String ip;
	@JsonProperty("mac")
	private String mac;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
}
