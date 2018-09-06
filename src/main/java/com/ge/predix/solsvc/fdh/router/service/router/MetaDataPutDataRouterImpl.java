/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.router.service.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.Data;
import com.ge.predix.entity.metadata.MetaData;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.entity.util.map.Entry;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.router.validator.RouterPutDataCriteriaValidator;
import com.ge.predix.solsvc.fdh.router.validator.RouterPutDataValidator;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author predix
 */
@Component(value = "metaDataPutFieldDataService")
public class MetaDataPutDataRouterImpl extends PutDataRouterImpl
{
    private static final Logger            log = LoggerFactory.getLogger(MetaDataPutDataRouterImpl.class);

    @Autowired
    private RouterPutDataValidator         validator;

    @Autowired
    private RouterPutDataCriteriaValidator criteriaValidator;

    @Autowired
    private ApplicationContext             context;

    @Autowired
    private RestClient                     restClient;

    @Autowired
    private JsonMapper                     mapper;

    /**
     * @param request
     *            -
     * @param headers
     *            -
     * @return -
     */
    @Override
    @SuppressWarnings(
    {
            "nls"
    })
    public PutFieldDataResult putData(PutFieldDataRequest request, Map<Integer, Object> modelLookupMap,
            List<Header> headers)
    {
       
        this.validator.validate(request);
        
        PutFieldDataRequest requestWithoutMetaData = createRequestwithOutMetaData(request);

        // 1. check for metadata , if not proceed to regular PutDataRequest
        PutFieldDataCriteria metaDataCriteria = checkAndDecorateMetaData(request);
        if ( metaDataCriteria == null )
        {
            return super.putData(request, modelLookupMap, headers);
        }
        // 2 . Call super put request for metadata construct a new put request
        PutFieldDataRequest metaDataRequest = new PutFieldDataRequest();
        metaDataRequest.getPutFieldDataCriteria().add(metaDataCriteria);
        // 3. Call the PutDataRequest for MetaData
        PutFieldDataResult metaDataResult = super.putData(metaDataRequest, modelLookupMap, headers);
        // 4. Call the PutDataRequest for DataFile
        if( ! CollectionUtils.isEmpty(requestWithoutMetaData.getPutFieldDataCriteria())) {
            PutFieldDataResult dataResults = super.putData(requestWithoutMetaData, modelLookupMap, headers);
            // 5. Call update on MetaData to update the uploaded data information
            updateMetaData(dataResults,metaDataCriteria,modelLookupMap, headers);
        }
        /// should we merge the results
        return metaDataResult;
    }

    /**
     * Method that Updates the metaData Asset with information about the Updated Asset
     * @param fullResult
     * @param datafileResult -
     * @param headers 
     * @param modelLookupMap 
     * @return -
     */
    @SuppressWarnings("unchecked")
    private void updateMetaData(PutFieldDataResult datafileResult, PutFieldDataCriteria metaDataCriteria, Map<Integer, Object> modelLookupMap, List<Header> headers)
    {
     
       List<String> uploadIds = new ArrayList<String>();
       if(datafileResult !=null && datafileResult.getExternalAttributeMap() !=null )
       {
           List<Entry> entries = datafileResult.getExternalAttributeMap().getEntry();
           for(Entry entry:entries){
               if(StringUtils.equalsIgnoreCase("uploadIds",entry.getKey().toString()) ){ //$NON-NLS-1$
                   uploadIds.add(entry.getValue().toString());
           }
       }
       }
       if(! CollectionUtils.isEmpty(uploadIds)) {
           for (@SuppressWarnings("unused") Field field : metaDataCriteria.getFieldData().getField())
           {
               Data data = metaDataCriteria.getFieldData().getData();
               if ( data instanceof MetaData )
               {
                 MetaData metaData = (MetaData) data;
                 if(metaData.getAdditionalAttributes() == null) {
                     metaData.setAdditionalAttributes(new com.ge.predix.entity.util.map.Map());
                 }
                 metaData.getAdditionalAttributes().put("uploadIds", uploadIds); //$NON-NLS-1$
                 metaDataCriteria.getFieldData().setData(metaData);
                 metaData.setProcessstatus("COMPLETED"); //$NON-NLS-1$
               }
    
           }
       }
       PutFieldDataRequest metaDataRequest = new PutFieldDataRequest();
       metaDataRequest.getPutFieldDataCriteria().add(metaDataCriteria);
       super.putData(metaDataRequest, modelLookupMap, headers);
    }

    /**
     * @param request
     * @return -
     */
    private PutFieldDataRequest createRequestwithOutMetaData(PutFieldDataRequest request)
    {
        PutFieldDataRequest requestWithOutMetaData = new PutFieldDataRequest();
        requestWithOutMetaData.setExternalAttributeMap(request.getExternalAttributeMap());
        
        for (PutFieldDataCriteria fieldDataCriteria : request.getPutFieldDataCriteria())
        {
            for (Field field : fieldDataCriteria.getFieldData().getField())
            {
                Data data = fieldDataCriteria.getFieldData().getData();

                if ( !(data instanceof MetaData) )
                {
                    requestWithOutMetaData.getPutFieldDataCriteria().add(fieldDataCriteria);

                }

            }
        }
        return requestWithOutMetaData;
    }

    /**
     * @param request
     * @return
     */
    private PutFieldDataCriteria checkAndDecorateMetaData(PutFieldDataRequest request)
    {

        for (PutFieldDataCriteria fieldDataCriteria : request.getPutFieldDataCriteria())
        {
            for (Field field : fieldDataCriteria.getFieldData().getField())
            {
                Data data = fieldDataCriteria.getFieldData().getData();

                if ( data instanceof MetaData )
                {
                    decorateMetaDataRequest(data);
                    field.getFieldIdentifier().setName(FieldSourceEnum.PREDIX_ASSET.name());
                    return fieldDataCriteria;

                }

            }
        }
        return null;
    }

    /**
     * Method to decorate the MetaData request to setup the asset Id and Asset URL
     * 
     * @param request -
     */
    private void decorateMetaDataRequest(Data data)
    {

        ((MetaData) data).setId(UUID.randomUUID().toString());
        ((MetaData) data).setUri("/data-exchange/" + ((MetaData) data).getId()); //$NON-NLS-1$
        ((MetaData) data).setTimestamp(Long.toString(System.currentTimeMillis()));
        ((MetaData) data).setProcessstatus("SUBMITTED"); //$NON-NLS-1$

    }

}
