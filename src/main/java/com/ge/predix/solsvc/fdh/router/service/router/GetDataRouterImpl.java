package com.ge.predix.solsvc.fdh.router.service.router;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.filter.Filter;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.model.Model;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.handler.FDHUtil;
import com.ge.predix.solsvc.fdh.handler.GetDataHandler;
import com.ge.predix.solsvc.fdh.router.validator.RouterGetDataValidator;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author predix -
 */
@Component(value = "getFieldDataService")
public class GetDataRouterImpl
        implements GetRouter, ApplicationContextAware
{
    private static final Logger    log = LoggerFactory.getLogger(GetDataRouterImpl.class);

    @Autowired
    private RouterGetDataValidator validator;

    private ApplicationContext     context;

    @Autowired
    private RestClient             restClient;

    @Autowired
    private JsonMapper             mapper;

    /**
     * This methods loops through the Requests for a given asset - and continues
     * through the same processing logic for a different asset. We need to start
     * transaction at this point here to ensure no lazy initialization exception
     * is thrown due to restoring the FieldMetaData objects.
     * 
     * @param getFieldDataRequest
     *            -
     * @return -
     */
    @Override
    public GetFieldDataResult getData(GetFieldDataRequest getFieldDataRequest, Map<Integer, Model> modelLookupMap,
            List<Header> headers)
    {
        this.validator.validate(getFieldDataRequest);
        GetFieldDataResult fullResult = processRequest(getFieldDataRequest, modelLookupMap, headers);
        return fullResult;
    }

    /**
     */
    @SuppressWarnings("nls")
    private GetFieldDataResult processRequest(GetFieldDataRequest request, Map<Integer, Model> modelLookupMap,
            List<Header> headers)
    {
        try
        {
            GetFieldDataResult fullResult = new GetFieldDataResult();
            for (FieldDataCriteria criteria : request.getFieldDataCriteria())
            {
                for (FieldSelection fieldSelection : criteria.getFieldSelection())
                {
                    try
                    {
                        // change the sourceName to the use the "handler/beanName" syntax
                        if ( fieldSelection.getFieldIdentifier() == null
                                || fieldSelection.getFieldIdentifier().getSource() == null )
                            throw new UnsupportedOperationException(
                                    "fieldSelection.getField().getFieldIdentifier() or fieldSelection.getField().getFieldIdentifier().getSource()=null not supported");

                        String sourceName = fieldSelection.getFieldIdentifier().getSource();
                        if ( sourceName == null ) throw new UnsupportedOperationException(
                                "fieldSelection.getField().getFieldIdentifier().getSource()=" + sourceName
                                        + " not supported");

                        if ( sourceName.startsWith("handler/") || sourceName.startsWith("http://")
                                || sourceName.startsWith("https://") )
                        {
                            //do nothing
                        }
                        else if ( sourceName.equals(FieldSourceEnum.PREDIX_ASSET.name()) )
                        {
                            fieldSelection.getFieldIdentifier().setSource("handler/assetGetFieldDataHandler");
                        }
                        else if ( sourceName.equals(FieldSourceEnum.PREDIX_TIMESERIES.name()) )
                        {
                            fieldSelection.getFieldIdentifier().setSource("handler/timeseriesGetDataHandler");
                        }
                        else
                            throw new UnsupportedOperationException(
                                    "fieldSelection.getField().getFieldIdentifier().getSource()=" + sourceName
                                            + " not supported");
                    }
                    catch (Throwable e)
                    {
                        // if one of the FieldSelection can't be routed/retrieved we'll continue with the others. The caller should submit compensating
                        // transactions.
                        String fieldString = null;
                        if ( criteria != null && criteria.getFieldSelection() != null )
                            fieldString = criteria.getFieldSelection().toString();
                        Filter filter = null;
                        if ( criteria != null && criteria.getFilter() != null ) filter = criteria.getFilter();
                        String msg = "unable to process request errorMsg=" + e.getMessage() + " request.correlationId="
                                + request.getCorrelationId() + " filter=" + filter + " for field=" + fieldString;
                        log.error(msg, e);
                        fullResult.getErrorEvent().add(msg);
                    }
                }
            }

            for (FieldDataCriteria criteria : request.getFieldDataCriteria())
            {
                try
                {
                    process(request, headers, fullResult, criteria);
                }
                catch (Throwable e)
                {
                    // if one of the FieldSelection can't be routed/retrieved we'll continue with the others. The caller should submit compensating
                    // transactions.
                    String fieldString = null;
                    if ( criteria != null && criteria.getFieldSelection() != null )
                        fieldString = criteria.getFieldSelection().toString();
                    Filter filter = null;
                    if ( criteria != null && criteria.getFilter() != null ) filter = criteria.getFilter();
                    String selection = null;
                    if ( criteria != null && criteria.getFieldSelection() != null)
                        selection = criteria.getFieldSelection().toString();
                    String msg = "unable to process request errorMsg=" + e.getMessage() + " request.correlationId="
                            + request.getCorrelationId() + "selection=" + selection  + " filter=" + filter + " for field=" + fieldString;
                    log.error(msg, e);
                    fullResult.getErrorEvent().add(msg);
                }
            }

            return fullResult;
        }
        catch (IllegalStateException e)
        {
            throw new RuntimeException("error in getFieldData", e);
        }
    }

    @SuppressWarnings("nls")
    private void process(GetFieldDataRequest request, List<Header> headers, GetFieldDataResult fullResult,
            FieldDataCriteria criteria)
    {
        for (FieldSelection fieldSelection : criteria.getFieldSelection())
        {

            String sourceName = fieldSelection.getFieldIdentifier().getSource();
            if ( sourceName.contains("handler/") )
            {
                GetFieldDataRequest singleRequest = makeSingleRequest(request, criteria, fieldSelection);
                processSingleHandler(singleRequest, headers, fullResult, criteria, fieldSelection);
            }
            else if ( sourceName.startsWith("http://") || sourceName.startsWith("https://") )
            {
                GetFieldDataRequest singleRequest = makeSingleRequest(request, criteria, fieldSelection);
                processRESTRequest(singleRequest, headers, fullResult, fieldSelection);
            }
            else
                throw new UnsupportedOperationException(
                        "fieldSelection.getField().getFieldIdentifier().getSource()=" + sourceName + " not supported");
        }
    }

    /**
     * Make the call to the Handler bean
     * 
     * @param singleRequest
     * @param headers
     * @param fullResult
     * @param criteria
     * @param fieldSelection
     *            -
     */
    @SuppressWarnings("nls")
    private void processSingleHandler(GetFieldDataRequest singleRequest, List<Header> headers,
            GetFieldDataResult fullResult, FieldDataCriteria criteria, FieldSelection fieldSelection)
    {
        List<Header> headersToUse = FDHUtil.copyHeaders(headers);
        headersToUse = setOverrideHeaders(criteria, headersToUse);

        String source = fieldSelection.getFieldIdentifier().getSource();
        if ( !source.contains("handler/") ) throw new UnsupportedOperationException(
                "please add the name of Spring Bean after handler  e.g. handler/sampleHandler");
        String beanName = source.substring(source.indexOf("handler/") + 8);
        beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);

        GetDataHandler bean = (GetDataHandler) this.context.getBean(beanName);
        // TODO - use the same model lookup map, used as a cache of models - needs research
        Map<Integer, Object> modelLookupMap = new HashMap<Integer, Object>();

        GetFieldDataResult singleResult = bean.getData(singleRequest, modelLookupMap, headersToUse);
        fullResult.getFieldData().addAll(singleResult.getFieldData());
    }

    /**
     * Mode 2: Supporting different ClientIds per DataSource in Data Exchange
     * (coming soon with APM release)
     * 
     * Constraints:
     * 
     * Data Sources typically have different ZoneIds. Data Sources might support
     * same ClientId or sometimes different ClientId. Data Sources might even
     * support a different UAA. Requests for multiple TenantIds will not be
     * supported. Thus, TenantId, Tokens and ZoneIds will be accepted in the
     * DataExchange API. ClientId and Secrets will not be accepted in API.
     * 
     * Require receiving Tenant Id in HTTP Header - pass Headers to SDKs, SDKs
     * send Headers in HTTP Request (TenantId not needed for Predix Time Series)
     * 
     * Support receiving Default Token in HTTP Header. If this token gets access
     * to all Data Sources, that is all that is needed.
     * 
     * Support receiving Default ZoneId in HTTP Header. If only one ZoneId is
     * needed for all Data Sources, that is all that is needed. If no VCAP
     * binding (Mode 1) is available, ZoneId is required.
     * 
     * Support receiving Override Token in the POST BODY - Put/Get Request
     * 
     * Support receiving Override ZoneId in the POST BODY - Put/Get Request. If
     * no VCAP binding (Mode 1) is available, ZoneId is required.
     * 
     * @param fieldDataCriteria
     * @param headers
     * @return -
     */
    @SuppressWarnings("nls")
    private List<Header> setOverrideHeaders(FieldDataCriteria criteria, List<Header> headers)
    {
        FDHUtil.setHeader(headers, criteria.getHeaders(), "Predix-Zone-Id");
        FDHUtil.setHeader(headers, criteria.getHeaders(), "Authorization");

        return headers;
    }

    private GetFieldDataRequest makeSingleRequest(GetFieldDataRequest request, FieldDataCriteria criteria,
            FieldSelection fieldSelection)
    {
        GetFieldDataRequest singleRequest = new GetFieldDataRequest();
        singleRequest.setCorrelationId(request.getCorrelationId());
        singleRequest.setExternalAttributeMap(request.getExternalAttributeMap());
        FieldDataCriteria singleFieldDataCriteria = new FieldDataCriteria();
        singleFieldDataCriteria.setNamespaces(criteria.getNamespaces());
        singleFieldDataCriteria.setHeaders(criteria.getHeaders());
        singleFieldDataCriteria.setResultId(criteria.getResultId());
        singleFieldDataCriteria.getFieldSelection().add(fieldSelection);
        singleFieldDataCriteria.setFilter(criteria.getFilter());
        singleRequest.getFieldDataCriteria().add(singleFieldDataCriteria);
        return singleRequest;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.context = applicationContext;
    }

    @SuppressWarnings(
    {
            "nls", "null"
    })
    private void processRESTRequest(GetFieldDataRequest singleRequest, List<Header> headers,
            GetFieldDataResult fullResult, FieldSelection fieldSelection)
    {
        GetFieldDataResult singleResult = null;

        String url = fieldSelection.getFieldIdentifier().getSource();
        String method = fieldSelection.getFieldIdentifier().getName();
        if ( url == null || !url.startsWith("http://") || !url.startsWith("https://") )
            throw new UnsupportedOperationException(
                    "please set the source to the url of REST service e.g. https://myservice.com");
        if ( "POST".equals(method.toUpperCase()) )
        {
            EntityBuilder builder = EntityBuilder.create();
            builder.setText(this.mapper.toJson(singleRequest));
            HttpEntity reqEntity = builder.build();
            try (CloseableHttpResponse response = this.restClient.post(url, reqEntity, headers, 100, 1000);)
            {
                String res = this.restClient.getResponse(response);
                singleResult = this.mapper.fromJson(res, GetFieldDataResult.class);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error when performing POST to Custom Rest Service : ", e);
            }
        }
        else if ( "GET".equals(method.toUpperCase()) )
        {
            try (CloseableHttpResponse response = this.restClient.get(url, null, 100, 1000);)
            {
                String res = this.restClient.getResponse(response);
                singleResult = this.mapper.fromJson(res, GetFieldDataResult.class);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error when performing GET to Custom Rest Service : ", e);
            }
        }
        fullResult.getFieldData().addAll(singleResult.getFieldData());
    }
}
