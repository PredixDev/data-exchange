package com.ge.predix.solsvc.fdh.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ge.predix.entity.asset.Asset;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.util.map.Map;
import com.ge.predix.solsvc.bootstrap.ams.dto.Attribute;
import com.ge.predix.solsvc.fdh.handler.asset.AssetPutDataHandlerImpl;
import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;

/**
 * @author tturner
 */
@SuppressWarnings({ "nls" })
@ActiveProfiles({ "local", "asset" })
@ComponentScan
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { AssetPutFieldDataTest.class, AssetPutDataHandlerImpl.class })
public class AssetPutFieldDataTest extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(AssetPutFieldDataTest.class.getName());
	private static final String HTTP_PAYLOAD_JSON = "application/json";
	private static final String CONTAINER_SERVER_PORT = "9092";

	private CloseableHttpResponse response;

	@Autowired
	@Qualifier("defaultOauthRestConfig")
	private IOauthRestConfig restConfig;

	/**
	 * @throws Exception
	 *             -
	 */
	@Before
	public void onSetUp() throws Exception {
		//
		// make sure the correct RestClient is wired to serviceBase
		// It gets changed by mock testing PredixAssetClient
		MockitoAnnotations.initMocks(this);

	}

	/**
	 * 
	 */
	@After
	public void onTearDown() {
		//
	}

	/**
	 * @throws JMSException
	 *             -
	 * @throws HttpException
	 *             -
	 * @throws IOException
	 *             -
	 * @throws JAXBException
	 *             -
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPut() throws HttpException, IOException, JAXBException {
		Long solutionId = 1000l;
		String namespace = "asset";
		String attributeName = "attribute1";
		String fieldId = namespace + "/" + attributeName;
		String fieldName = namespace + "/" + attributeName;
		String fieldSource = FieldSourceEnum.PREDIX_ASSET.name();

		double rawDataValue = 52.1d;
		String assetId = "12345";

		Date now = new Date();

		List<Asset> assets = new ArrayList<Asset>();
		Asset asset = new Asset();
		asset.setAssetId("12345");
		asset.setUri("/asset/getrb_2");
		asset.setAttributes(new Map());
		Attribute attribute = new Attribute();
		attribute.getValue().add("value");
		asset.getAttributes().put(attributeName, attribute);
		assets.add(asset);


		List<Header> headers = new ArrayList<Header>();
		Header header = new BasicHeader("Content-Type", "application/json");
		headers.add(header);


		putFieldData(solutionId, fieldId, fieldName, fieldSource, rawDataValue, assetId, now, log, HTTP_PAYLOAD_JSON,
				CONTAINER_SERVER_PORT);

	}

}
