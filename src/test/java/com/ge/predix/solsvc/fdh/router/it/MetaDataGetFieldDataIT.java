package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ge.predix.entity.assetfilter.AssetFilter;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.metadata.MetaData;
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
@SpringApplicationConfiguration(classes = { FdhRouterApplication.class, AssetGetFieldDataHandlerImpl.class })
@WebAppConfiguration
@IntegrationTest({"server.port=9092"})
public class MetaDataGetFieldDataIT
{

    @SuppressWarnings("unused")
    private static final Logger     log = LoggerFactory.getLogger(MetaDataGetFieldDataIT.class);

    @Autowired
    private RestClient                    restClient;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    @Qualifier("defaultOauthRestConfig")
	private IOauthRestConfig restConfig;


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
    @SuppressWarnings(
    {
    })
    @Before
    public void setUp()
            throws Exception
    {
        //
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
     *  -
     * @throws IOException  -
     */
    @Test
    public void testGetMetaDataList() throws IOException
    {
        log.debug("================================"); //$NON-NLS-1$
        String assetUri = "/data-exchange";
        String selection = "/MetaData";
        //GetFieldDataRequest request = TestData.getDataExchangeMetaDataRequest("/data-exchange"); //$NON-NLS-1$
        
        GetFieldDataRequest request = TestData.createGetAssetRequest(assetUri, selection, "MetaData");
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/getfielddata";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        log.debug("URL = " + url);//$NON-NLS-1$
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));//$NON-NLS-1$ //$NON-NLS-2$
        this.restClient.addSecureTokenForHeaders(headers);
        log.debug("REQUEST: Input json to get field data = " + this.jsonMapper.toJson(request));//$NON-NLS-1$
        
        String aJson = this.jsonMapper.toJson(request);
        GetFieldDataRequest request1 = this.jsonMapper.fromJson(aJson, GetFieldDataRequest.class);
        
        log.info("request1 = " + request1);//$NON-NLS-1$
        CloseableHttpResponse response = null;
        try {
            response = this.restClient.post(url, this.jsonMapper.toJson(request), headers, 1000000, 1000000);
            
            log.debug("RESPONSE: Response from Get Field Data  = " + response);//$NON-NLS-1$
    
            String responseAsString = this.restClient.getResponse(response);
            log.debug("RESPONSE: Response from Get Field Data  = " + responseAsString);//$NON-NLS-1$
    
            Assert.assertNotNull(response);
            Assert.assertNotNull(responseAsString);
            Assert.assertFalse(responseAsString.contains("\"status\":500"));//$NON-NLS-1$
            Assert.assertFalse(responseAsString.contains("Internal Server Error"));//$NON-NLS-1$
            Assert.assertTrue(responseAsString.contains("errorEvent\":[]"));//$NON-NLS-1$
            GetFieldDataResult getFieldDataResponse = this.jsonMapper.fromJson(responseAsString, GetFieldDataResult.class);
            @SuppressWarnings({
                    "unused", "rawtypes"
            })
            List fieldDate = getFieldDataResponse.getFieldData();
            System.out.println("In here"); //$NON-NLS-1$
           // Assert.assertTrue(getFieldDataResponse.getFieldData().get(0).getData() instanceof DatapointsResponse);
          //  DatapointsResponse dpResponse = (DatapointsResponse) getFieldDataResponse.getFieldData().get(0).getData();
           // Assert.assertTrue(dpResponse.getTags().size() > 0);
           // Assert.assertTrue(dpResponse.getTags().get(0).getStats().getRawCount() > 0);
        }catch (Exception e) {
            log.debug("Error  ", e);//$NON-NLS-1
        }
        finally {
            if(response!=null)
                response.close();
                
        } 
        
    }
    

    
    /**
     *  -
     * @throws IOException  -
     * 
     */
    public void testDeleteMetaDataList() throws IOException
    {
        log.debug("================================"); //$NON-NLS-1$
        String assetUri = "/data-exchange";
        String selection = "/MetaData";
        //GetFieldDataRequest request = TestData.getDataExchangeMetaDataRequest("/data-exchange"); //$NON-NLS-1$
        
        GetFieldDataRequest request = TestData.createGetAssetRequest(assetUri, selection, "MetaData");
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/getfielddata";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        log.debug("URL = " + url);//$NON-NLS-1$
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));//$NON-NLS-1$ //$NON-NLS-2$
        this.restClient.addSecureTokenForHeaders(headers);
        log.debug("REQUEST: Input json to get field data = " + this.jsonMapper.toJson(request));//$NON-NLS-1$
        
        String aJson = this.jsonMapper.toJson(request);
        GetFieldDataRequest request1 = this.jsonMapper.fromJson(aJson, GetFieldDataRequest.class);
        
        log.info("request1 = " + request1);//$NON-NLS-1$
        CloseableHttpResponse response = null;
        try {
            response = this.restClient.post(url, this.jsonMapper.toJson(request), headers, 1000000, 1000000);
            
            log.debug("RESPONSE: Response from Get Field Data  = " + response);//$NON-NLS-1$
    
            String responseAsString = this.restClient.getResponse(response);
            log.debug("RESPONSE: Response from Get Field Data  = " + responseAsString);//$NON-NLS-1$
            
            GetFieldDataResult getFieldDataResponse = this.jsonMapper.fromJson(responseAsString, GetFieldDataResult.class);
           
            url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/getfielddata";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            headers = new ArrayList<Header>();
            headers.add(new BasicHeader("Content-Type", "application/json"));//$NON-NLS-1$ //$NON-NLS-2$
           // headers.add(new BasicHeader("Predix-Zone-Id", "e182cece-ee12-42a0-878e-0ba0aef3d877"));//$NON-NLS-1$ //$NON-NLS-2$
            this.restClient.addSecureTokenForHeaders(headers);
           
            List<FieldData> fieldDates = getFieldDataResponse.getFieldData();
          
            for (FieldData fieldDate:fieldDates ){
                MetaData metadata = (MetaData)fieldDate.getData();
                log.info("Deleting"+"https://predix-asset.run.aws-usw02-pr.ice.predix.io"+metadata.getUri());
                this.restClient.delete("https://predix-asset.run.aws-usw02-pr.ice.predix.io"+metadata.getUri(), headers, 1000000, 1000000);
            }
        }catch (Exception e) {
            log.debug("Error  ", e);//$NON-NLS-1
        }
        finally {
            if(response!=null)
                response.close();
                
        } 
        
    }
    
    
    
}
