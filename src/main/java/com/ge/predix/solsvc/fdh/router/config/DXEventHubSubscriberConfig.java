package com.ge.predix.solsvc.fdh.router.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.restclient.config.DefaultOauthRestConfig;

/**
 * 
 * @author 212546387 -
 */
@Component("dxEventHubSubscriberConfig")
@Profile("eventhub")
public class DXEventHubSubscriberConfig extends DefaultOauthRestConfig{
	
	@Value("${predix.eventhub.subscribe.zoneid:#{null}}")
	private String eventHubZoneId;

	@Value("${predix.eventhub.subscribe.host:#{null}}")
	private String eventHubHostName;
	
	@Value("${predix.eventhub.subscribe.service.name:#{null}}")
	private String eventHubServiceName;
	
	@Value("${predix.eventhub.uaa.service.name:#{null}}")
	private String eventHubUAAServiceName;
	
	@Value("${predix.eventhub.subscribe.topic:#{null}}")
	private String subscribeTopic;
	
	/**
	 * @return -
	 */
	public String getEventHubZoneId() {
		return this.eventHubZoneId;
	}

	/**
	 * @param eventHubZoneId -
	 */
	public void setEventHubZoneId(String eventHubZoneId) {
		this.eventHubZoneId = eventHubZoneId;
	}

	/**
	 * @return -
	 */
	public String getEventHubServiceName() {
		return this.eventHubServiceName;
	}

	/**
	 * @param eventHubServiceName -
	 */
	public void setEventHubServiceName(String eventHubServiceName) {
		this.eventHubServiceName = eventHubServiceName;
	}

	/**
	 * @return -
	 */
	public String getEventHubUAAServiceName() {
		return this.eventHubUAAServiceName;
	}

	/**
	 * @param eventHubUAAServiceName -
	 */
	public void setEventHubUAAServiceName(String eventHubUAAServiceName) {
		this.eventHubUAAServiceName = eventHubUAAServiceName;
	}

	/**
	 * @return -
	 */
	public String getEventHubHostName() {
		return this.eventHubHostName;
	}

	/**
	 * @param eventHubHostName -
	 */
	public void setEventHubHostName(String eventHubHostName) {
		this.eventHubHostName = eventHubHostName;
	}

	/**
	 * @return -
	 */
	public String getSubscribeTopic() {
		return this.subscribeTopic;
	}

	/**
	 * @param subscribeTopic -
	 */
	public void setSubscribeTopic(String subscribeTopic) {
		this.subscribeTopic = subscribeTopic;
	}
}
