package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.handler.asset.AssetGetFieldDataHandlerImpl;
import com.ge.predix.solsvc.fdh.handler.timeseries.TimeseriesGetDataHandler;
import com.ge.predix.solsvc.fdh.router.boot.DataExchangeRouterApplication;
import com.ge.predix.solsvc.fdh.router.service.router.GetRouter;
import com.ge.predix.solsvc.fdh.router.util.TestData;
import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.timeseries.bootstrap.client.TimeseriesClient;

/**
 * 
 * @author predix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { DataExchangeRouterApplication.class, AssetGetFieldDataHandlerImpl.class,
		TimeseriesGetDataHandler.class, GetRouter.class })
@WebAppConfiguration
@IntegrationTest({ "server.port=9092" })
@ActiveProfiles({ "asset", "timeseries" })
public class TimeseriesIT {

	private static final Logger log = LoggerFactory.getLogger(TimeseriesIT.class);

    @Autowired
    private TimeseriesClient timeseriesClient;
    
    @Autowired
    private RestClient restClient;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	@Qualifier("defaultOauthRestConfig")
	private IOauthRestConfig restConfig;

	/**
	 * @throws Exception
	 *             -
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// new RestTemplate();
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@SuppressWarnings({})
	@Before
	public void setUp() throws Exception {
		this.jsonMapper.init();
	}

	/**
	 * @throws Exception
	 *             -
	 */
	@After
	public void tearDown() throws Exception {
		//
	}

	/**
	 * @throws IOException
	 *             -
	 * @throws IllegalStateException
	 *             -
	 */
	@SuppressWarnings({ "nls" })
	@Test
	public void testCreateDatapoint() throws IllegalStateException, IOException {
	
		log.debug("================================");
		String sensorName = "crank-frame-velocity";
		String assetId = "compressor-2017";
	
		PutFieldDataRequest request = TestData.putFieldDataRequest(assetId, sensorName, 0, 10);
	
		String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/putfielddata";
		log.debug("URL = " + url);
	
		List<Header> headers = this.timeseriesClient.getTimeseriesHeaders();
		headers.add(new BasicHeader("Content-Type", "application/json"));
		log.debug("REQUEST: Input json to get field data = " + this.jsonMapper.toJson(request));
	
		CloseableHttpResponse response = null;
		try {
			response = this.restClient.post(url, this.jsonMapper.toJson(request), headers,
					this.restConfig.getDefaultConnectionTimeout(), this.restConfig.getDefaultSocketTimeout());
	
			log.info("RESPONSE: Response from Put Field Data  = " + response);
	
			String responseAsString = this.restClient.getResponse(response);
			log.info("RESPONSE: Response from Put Field Data  = " + responseAsString);
	
			Assert.assertNotNull(response);
			Assert.assertNotNull(responseAsString);
			Assert.assertFalse(responseAsString.contains("\"status\":500"));
			Assert.assertFalse(responseAsString.contains("Internal Server Error"));
			Assert.assertTrue(responseAsString.contains("errorEvent\":[]"));
		} finally {
			if (response != null)
				response.close();
	
		}
	}

	/**
	 * @throws IllegalStateException
	 *             -
	 * @throws IOException
	 *             -
	 */
	@Test
	public void testGetDatapointByTimeSeriesOnly() throws IllegalStateException, IOException {
		testGetFieldData("GetFieldDataRequestWithTS.json"); //$NON-NLS-1$
	}

	/**
	 * @throws IllegalStateException
	 *             -
	 * @throws IOException
	 *             -
	 */
	@Test
	public void testGetDatapointByAssetCriteriaAwareTimeSeries() throws IllegalStateException, IOException {
		testGetFieldData("GetFieldDataRequestAssetAndTS.json"); //$NON-NLS-1$
	}

	private void testGetFieldData(String requestFile) throws IllegalStateException, IOException {

		log.debug("================================"); //$NON-NLS-1$
		String request = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(requestFile));

		String url = "http://localhost:" + "9092" //$NON-NLS-1$ //$NON-NLS-2$
				+ "/services/fdhrouter/fielddatahandler/getfielddata"; //$NON-NLS-1$
		log.debug("URL = " + url); //$NON-NLS-1$

		List<Header> headers = this.timeseriesClient.getTimeseriesHeaders();
		headers.add(new BasicHeader("Content-Type", "application/json")); //$NON-NLS-1$ //$NON-NLS-2$
		this.timeseriesClient.addSecureTokenToHeaders(headers);

		CloseableHttpResponse response = null;
		try {
			response = this.restClient.post(url, request, headers, 1000000, 1000000);

			log.debug("RESPONSE: Response from Get Field Data  = " + response); //$NON-NLS-1$

			String responseAsString = this.restClient.getResponse(response);
			log.debug("RESPONSE: Response from Get Field Data  = " //$NON-NLS-1$
					+ responseAsString);

			Assert.assertNotNull(response);
			Assert.assertNotNull(responseAsString);
			Assert.assertFalse(responseAsString.contains("\"status\":500")); //$NON-NLS-1$
			Assert.assertFalse(responseAsString.contains("Internal Server Error")); //$NON-NLS-1$
			Assert.assertTrue(responseAsString.contains("errorEvent\":[]")); //$NON-NLS-1$
			GetFieldDataResult getFieldDataResponse = this.jsonMapper.fromJson(responseAsString,
					GetFieldDataResult.class);
			Assert.assertTrue(getFieldDataResponse.getFieldData().get(0).getData() instanceof DatapointsResponse);
			DatapointsResponse dpResponse = (DatapointsResponse) getFieldDataResponse.getFieldData().get(0).getData();
			Assert.assertTrue(dpResponse.getTags().size() > 0);
			log.info("Status Count : " + dpResponse.getTags().get(0).getStats().getRawCount()); //$NON-NLS-1$
			// Assert.assertTrue(dpResponse.getTags().get(0).getStats()
			// .getRawCount() > 0);
		} finally {
			if (response != null)
				response.close();

		}

	}
}
