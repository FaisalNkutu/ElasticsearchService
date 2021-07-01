package com.adtran.mosaicone.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Services {
	@JsonProperty("ips")
	private Ip[] ips = new Ip[2];

	public Ip[] getIps() {
		return ips;
	}

	public void setIps(Ip[] ips) {
		this.ips = ips;
	}
}
