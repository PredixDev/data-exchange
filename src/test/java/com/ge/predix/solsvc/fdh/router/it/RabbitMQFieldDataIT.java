package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.List;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.handler.rabbitmq.RabbitMQHandler;
import com.ge.predix.solsvc.fdh.router.boot.FdhRouterApplication;
import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;

import org.springframework.amqp.rabbit.junit.BrokerRunning;
/**
 * 
 * @author predix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes =
{
        FdhRouterApplication.class, RabbitMQHandler.class
})
@WebAppConfiguration
@IntegrationTest(
{
        "server.port=9092"
})
@ActiveProfiles(
{
        "rabbitmq"
})
public class RabbitMQFieldDataIT
{

    private static final Logger log             = LoggerFactory.getLogger(RabbitMQFieldDataIT.class);
        
    @Autowired
    private RestClient          restClient;

    @Autowired
    private JsonMapper          jsonMapper;

    @Autowired
    @Qualifier("defaultOauthRestConfig")
    private IOauthRestConfig    restConfig;

    private RestTemplate        template;
    
    @Autowired
    private RabbitMQHandler rabbitMQPutFieldDataHandler;

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
    public void testPutFieldDataToQ()
            throws IOException
    {
    	String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("PutFieldDataWithFieldChangedEventAsString.json"));
    	
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
            //Assert.assertTrue(body.contains("errorEvent\":[]"));
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
    public void testPutFieldDataWithhiloThresholdToQ()
            throws IOException
    {
    	String request = IOUtils.toString(getClass().getClassLoader()
				.getResourceAsStream("PutFieldDataUpdatehiLoThreshold-rmq.json"));
    	
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
            //Assert.assertTrue(body.contains("errorEvent\":[]"));
        }
        finally
        {
            if ( response != null ) response.close();
        }

    }
}
