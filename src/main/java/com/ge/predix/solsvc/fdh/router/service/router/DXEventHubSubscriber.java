package com.ge.predix.solsvc.fdh.router.service.router;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.ge.predix.eventhub.EventHubClientException;
import com.ge.predix.eventhub.Message;
import com.ge.predix.eventhub.client.Client;
import com.ge.predix.eventhub.configuration.EventHubConfiguration;
import com.ge.predix.eventhub.configuration.SubscribeConfiguration;
import com.ge.predix.solsvc.fdh.router.config.DXEventHubSubscriberConfig;

/**
 * Time series Handler - This handler supports 2 Filters. A pure
 * TimeseriesFilter and a AssetCriteriaAwareTimeseriesFilter.
 * 
 * TimeseriesFilter - simply takes the Time series requests and forwards it on
 * to Time series SDK AssetCriteriaAwareTimeseriesFilter -invokes Asset service
 * to get attributes and then replaces the values where it find {{replaceMe}}
 * mustache templates
 * 
 * After it receives the time series data it adapts it to the expectedDataType
 * in the expectedEngineeringUnits
 * 
 * @author predix
 */
@Component
@Profile("eventhub")
public class DXEventHubSubscriber {
	private static final Logger log = LoggerFactory.getLogger(DXEventHubSubscriber.class.getName());

	private Client synchClient;

	/**
	 * 
	 */
	public static final long DEFAULT_TIMEOUT = 60000L;

	/**
	 * 
	 */
	long SUBSCRIBER_ACTIVE_WAIT_LENGTH = 10000L;

	
	@Autowired
	private DXEventHubSubscriberConfig dxEventHubSubscriberConfig;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	private SubscribeCallback callback = new SubscribeCallback();
	
	/**
	 *  -
	 */
	@PostConstruct
	public void init() {
		try {
			this.taskExecutor.setCorePoolSize(5);
			this.taskExecutor.setMaxPoolSize(10);
			createClient();
			this.synchClient.subscribe(this.callback);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Exception when initiating conenction to eventhub", e); //$NON-NLS-1$
		}
	}

	/**
	 *  -
	 */
	@Scheduled(fixedRate=10000L)
	public void processMessage() {
		DXEventHubSubscriberProcessor processor = new DXEventHubSubscriberProcessor(this.callback.getMessage());
		this.taskExecutor.execute(processor);
	}
	/**
	 * @throws UnsupportedEncodingException -
	 */
	public void createClient() throws UnsupportedEncodingException {
		// make the async and sync clients
		try {
			String[] client = this.dxEventHubSubscriberConfig.getOauthClientId().split(":"); //$NON-NLS-1$
			EventHubConfiguration eventHubConfiguration = null;
			SubscribeConfiguration.Builder subscribeConfigBuilder = new SubscribeConfiguration.Builder();
			if (this.dxEventHubSubscriberConfig.getSubscribeTopic() != null
					&& !"".equals(this.dxEventHubSubscriberConfig.getSubscribeTopic())) { //$NON-NLS-1$
				//subscribeConfigBuilder.topic(this.eventHubConfig.getSubscribeTopic());
			}
			//if ((this.eventHubConfig.getEventHubServiceName() != null && !"".equals(this.eventHubConfig.getEventHubServiceName())
					//|| (this.eventHubConfig.getEventHubUAAServiceName() != null
					//		&& !"".equals(this.eventHubConfig.getEventHubUAAServiceName())))) {
				eventHubConfiguration = new EventHubConfiguration.Builder()
						.fromEnvironmentVariables(this.dxEventHubSubscriberConfig.getEventHubServiceName(),
								this.dxEventHubSubscriberConfig.getEventHubUAAServiceName())
						.clientID(client[0]).clientSecret(client[1])
						.subscribeConfiguration(subscribeConfigBuilder.build()).automaticTokenRenew(true).build();
			/*} else {
				eventHubConfiguration = new EventHubConfiguration.Builder().host(this.eventHubConfig.getEventHubHostName())
						.clientID(client[0]).clientSecret(client[1]).zoneID(this.eventHubConfig.getEventHubZoneId())
						.authURL(this.eventHubConfig.getOauthIssuerId())
						.subscribeConfiguration(subscribeConfigBuilder.build()).automaticTokenRenew(true).build();
			}*/

			this.synchClient = new Client(eventHubConfiguration);

		} catch (EventHubClientException.InvalidConfigurationException e) {
			log.error("*** Could not make client ***", e); //$NON-NLS-1$
			throw new RuntimeException("Could not make event hub client"); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 * @author 212546387 -
	 */
	public static class SubscribeCallback implements Client.SubscribeCallback {
		private List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
		private AtomicInteger errorCount = new AtomicInteger();
		private CountDownLatch finishLatch = new CountDownLatch(0);

		/**
		 * @param count -
		 */
		public void block(int count) {
			block(count, DEFAULT_TIMEOUT);
		}

		/**
		 * @param count -
		 * @param timeout -
		 */
		public void block(int count, long timeout) {
			if (getMessageCount() >= count) {
				return;
			}
			this.finishLatch = new CountDownLatch(count - getMessageCount());
			System.out.println("block started with count of: " + this.finishLatch.getCount()); //$NON-NLS-1$

			try {
				this.finishLatch.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("block finished with count of: " + this.finishLatch.getCount()); //$NON-NLS-1$
		}

		@Override
		public void onMessage(Message message) {
			// System.out.println(message.toString());
			// if (message.getBody().toStringUtf8().equals(expectedMessage)) {
			// System.out.println(System.currentTimeMillis() + "\n" +
			// message.toString()+"\n");
			// System.out.println(String.format("%s::message::%s::%s", name,
			// message.getId(), message.getBody().toStringUtf8()));
			this.messages.add(message);
			if (this.finishLatch.getCount() != 0) {
				this.finishLatch.countDown();
			}
			// }
		}

		@Override
		public void onFailure(Throwable throwable) {
			// System.out.println(name + "::error");
			// System.out.println(throwable.getMessage());
			this.errorCount.incrementAndGet();
		}

		/**
		 * @return -
		 */
		public int getMessageCount() {
			return this.messages.size();
		}

		/**
		 * @return -
		 */
		public List<Message> getMessage() {
			return this.messages;
		}

		/**
		 * @return -
		 */
		public int getErrorCount() {
			return this.errorCount.get();
		}

		/**
		 *  -
		 */
		public void resetCounts() {
			this.messages.clear();
			this.errorCount.set(0);
		}
	}
}
