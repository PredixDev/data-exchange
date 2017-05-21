package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.helpers.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mimosa.osacbmv3_3.DAString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.ge.predix.entity.fielddata.OsaData;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.handler.asset.AssetGetFieldDataHandlerImpl;
import com.ge.predix.solsvc.fdh.router.boot.FdhRouterApplication;
import com.ge.predix.solsvc.fdh.router.util.TestData;
import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author predix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes =
{
        FdhRouterApplication.class, AssetGetFieldDataHandlerImpl.class
})
@WebAppConfiguration
@IntegrationTest(
{
        "server.port=9092"
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssetFieldDataIT
{

    private static final Logger log             = LoggerFactory.getLogger(AssetFieldDataIT.class);

//    private static final String ASSET_TEST_FILE = "src/test/resources/WTG.json";                      //$NON-NLS-1$
    private static final String ASSET_TEST_FILE = "src/test/resources/asset-model.json";                      //$NON-NLS-1$

    @Autowired
    private RestClient          restClient;

    @Autowired
    private JsonMapper          jsonMapper;

    @Autowired
    @Qualifier("defaultOauthRestConfig")
    private IOauthRestConfig    restConfig;

    private RestTemplate        template;

    /**
     * @throws Exception -
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception
    {
        //
       
    }

    /**
     * @throws Exception -
     */
    @AfterClass
    public static void tearDownAfterClass()
            throws Exception
    {
        //
    }

    /**
     * @throws Exception -
     */
    @SuppressWarnings({})
    @Before
    public void setUp()
            throws Exception
    {
        this.template =new TestRestTemplate();
        this.template.getMessageConverters().add(new FormHttpMessageConverter());
        this.template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    /**
     * @throws Exception -
     */
    @After
    public void tearDown()
            throws Exception
    {
        //
    }

    /**
     * @throws IOException -
     */
    @SuppressWarnings("nls")
    @Test
    public void testAPutFieldData()
            throws IOException
    {
    	// Put value 20.55 ...value is in PutFieldData..json file
    	String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("PutFieldDataOutputMaximumRequest.json"));
    	
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/putfielddata";
        
        List<Header> headers = this.restClient.getSecureTokenForClientId();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        CloseableHttpResponse response = null;
        try
        {
            response = this.restClient.post(url, request, headers,
                    this.restConfig.getDefaultConnectionTimeout(), this.restConfig.getDefaultSocketTimeout());

            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("HTTP/1.1 200 OK"));
            String body = EntityUtils.toString(response.getEntity());
            Assert.assertTrue(body.contains("errorEvent\":[]"));
        }
        finally
        {
            if ( response != null ) response.close();
        }

    }
    
    /**
     * @throws IOException -
     */
    @SuppressWarnings("nls")
    @Test
    public void testCPutFieldDataWithFieldChangedEvent()
            throws IOException
    {
    	// Put value 20.55 ...value is in PutFieldData..json file
    	String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("PutFieldDataWithFieldChangedEvent.json"));
    	
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/putfielddata";
        
        List<Header> headers = this.restClient.getSecureTokenForClientId();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        CloseableHttpResponse response = null;
        try
        {
            response = this.restClient.post(url, request, headers,
                    this.restConfig.getDefaultConnectionTimeout(), this.restConfig.getDefaultSocketTimeout());

            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("HTTP/1.1 200 OK"));
            String body = EntityUtils.toString(response.getEntity());
            Assert.assertTrue(body.contains("errorEvent\":[]"));
        }
        finally
        {
            if ( response != null ) response.close();
        }

    }
    
    /**
     * @throws IOException -
     */
    @SuppressWarnings("nls")
    @Test
    public void testBGetFieldData()
            throws IOException
    {
    	String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("GetFieldDataOutputMaximumRequest.json"));
    	
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/getfielddata";

        List<Header> headers = this.restClient.getSecureTokenForClientId();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        CloseableHttpResponse response = null;
        try
        {
        	// Get value 20.55
            response = this.restClient.post(url, request, headers,
                    this.restConfig.getDefaultConnectionTimeout(), this.restConfig.getDefaultSocketTimeout());

            String responseAsString = this.restClient.getResponse(response);
            log.info("RESPONSE: Response from Get Field Data  = "
					+ responseAsString);
            
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("HTTP/1.1 200 OK"));
            Assert.assertFalse(responseAsString.contains("\"status\":500"));
			Assert.assertFalse(responseAsString
					.contains("Internal Server Error"));
			Assert.assertTrue(responseAsString.contains("errorEvent\":[]"));
			
			GetFieldDataResult getFieldDataResponse = this.jsonMapper.fromJson(
					responseAsString, GetFieldDataResult.class);
			
			Assert.assertTrue(getFieldDataResponse.getFieldData().get(0)
					.getData() instanceof OsaData);
			
			OsaData data = (OsaData) getFieldDataResponse.getFieldData().get(0)
					.getData();
			String valueStr = ((DAString) data.getDataEvent()).getValue();
			log.info("value = " + valueStr);	
			
			Assert.assertEquals("20.77", valueStr);
        }
        finally
        {
            if ( response != null ) response.close();
        }

    }

    /**
     * -
     * 
     * @throws IOException -
     */
    @SuppressWarnings("nls")
    @Test
    public void testDPutFieldDataAlertStatus()
            throws IOException
    {
        PutFieldDataRequest request = TestData.putFieldDataRequestSetAlertStatus();

        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/putfielddata";

        List<Header> headers = this.restClient.getSecureTokenForClientId();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        CloseableHttpResponse response = null;
        try
        {
            response = this.restClient.post(url, this.jsonMapper.toJson(request), headers,
                    this.restConfig.getDefaultConnectionTimeout(), this.restConfig.getDefaultSocketTimeout());

            Assert.assertNotNull(response);
            log.debug("response=" + response.toString());
            Assert.assertTrue(response.toString().contains("HTTP/1.1 200 OK"));
            String body = EntityUtils.toString(response.getEntity());
            log.debug("body=" + body);
            log.debug("herehere");
            Assert.assertTrue(body.contains("errorEvent\":[]"));
        }
        finally
        {
            if ( response != null ) response.close();
        }

    }

    /**
     * -
     * 
     * @throws IOException -
     */
    @SuppressWarnings("nls")
    @Test
    public void testEPutMetaDataWithAssetDateFile()
            throws IOException
    {
        PutFieldDataRequest request = TestData.putMetaDataWithAssetDataFileRequest();

        List<Header> headers = this.restClient.getSecureTokenForClientId();
        MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<String, String>();
        for (Header header : headers)
        {
            if ( StringUtils.startsWithIgnoreCase(header.getName(), "authorization") )
            {
                multiHeaders.add(header.getName(), header.getValue());
                break;
            }
        }

        multiHeaders.add("Content-Type", MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", new FileSystemResource(ASSET_TEST_FILE));
        String body = this.jsonMapper.toJson(request);
        log.info("payload to the upload " + body);
        parts.add("putfielddata", body);

        HttpEntity<?> httpEntity = new HttpEntity<Object>(parts, multiHeaders);

        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/putfielddatafile";

        PutFieldDataResult response = this.template.postForObject(url, httpEntity, PutFieldDataResult.class);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getErrorEvent() != null);
               
    }
}
