package com.adtran.mosaicone.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MosaicOneProductDocument {

	@JsonProperty("subscriber_code")
    private String subscriber_code;

	public String getSubscriber_code() {
		return subscriber_code;
	}

	public void setSubscriber_code(String subscriber_code) {
		this.subscriber_code = subscriber_code;
	}
	@JsonProperty("tenant_id")
	private String tenant_id;
	
	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getWanIPv4Address() {
		return wanIPv4Address;
	}

	public void setWanIPv4Address(String wanIPv4Address) {
		this.wanIPv4Address = wanIPv4Address;
	}

	public String getWanMacAddress() {
		return wanMacAddress;
	}

	public void setWanMacAddress(String wanMacAddress) {
		this.wanMacAddress = wanMacAddress;
	}
	@JsonProperty("subscriber_name")
    private String subscriberName;
	public String getSubscriberName() {
		return subscriberName;
	}

	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}

	public String getSubscriber_name() {
		return subscriber_name;
	}

	public void setSubscriber_name(String subscriber_name) {
		this.subscriber_name = subscriber_name;
	}
	//@JsonProperty("wan_mac_address_with_colons")
    //private String wan_mac_address_with_colons;
	
	//device Manager
	@JsonProperty("wanIPv4Address")
    private String wanIPv4Address;
	@JsonProperty("subscriberCode")
    private String subscriberCode;
	public String getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(String subscriberCode) {
		this.subscriberCode = subscriberCode;
	}
	//@JsonProperty("subscriberName")
    //private String subscriberName;	
	@JsonProperty("wanMacAddress")
	private String wanMacAddress;
	@JsonProperty("fullName")
    private String subscriber_name;	
	String index;
	//subscriber-insight
	@JsonProperty("full_name")
    private String full_name;	
	
	@JsonProperty("services")
	private Services[] services = new Services[1];

	public Services getServices() {
		return services[0];
	}

	public void setServices(Services[] services) {
		this.services = services;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	
}
