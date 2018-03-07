package com.ge.predix.solsvc.fdh.router.service.router;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.util.map.DataMap;
import com.ge.predix.eventhub.Message;
import com.ge.predix.solsvc.ext.util.JsonMapper;

/**
 * 
 * @author 212546387 -
 */
public class DXEventHubSubscriberProcessor implements Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(DXEventHubSubscriberProcessor.class);
	
	private List<Message> messages;
	
	@Autowired
	private PutRouter putFieldDataService;

	@Autowired
	private GetRouter getFieldDataService;
	
	@Autowired
	private JsonMapper mapper;
	
	/**
	 * @param messages -
	 */
	public DXEventHubSubscriberProcessor(List<Message> messages) {
		this.messages = messages;
	}
	
	@Override
	public void run() {
		log.info("no of messages : " + this.messages.size()); //$NON-NLS-1$
		for (Message message : this.messages) {
			String msg = message.getBody().toStringUtf8();
			DatapointsIngestion timeseriesData = this.mapper.fromJson(msg, DatapointsIngestion.class);
			if (timeseriesData != null) {
				PutFieldDataRequest request = createTimeseriesPutFieldDataRequest(timeseriesData);
				this.putFieldDataService.putData(request, null, null);
			} else {
				PutFieldDataRequest request = this.mapper.fromJson(msg, PutFieldDataRequest.class);
				if (request != null) {
					this.putFieldDataService.putData(request, null, null);
				} else {
					GetFieldDataRequest getRequest = this.mapper.fromJson(msg, GetFieldDataRequest.class);
					if (getRequest != null) {
						GetFieldDataResult result = this.getFieldDataService.getData(getRequest, null, null);
						// Create PutField Request to Send back the
						// request.
						PutFieldDataRequest sendResponseRequest = createPutFieldDataRequest(result);
						this.putFieldDataService.putData(sendResponseRequest, null, null);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private PutFieldDataRequest createPutFieldDataRequest(GetFieldDataResult result) {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		PutFieldDataCriteria criteria = new PutFieldDataCriteria();
		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();

		fieldIdentifier.setSource("handler/eventHubPutFieldDataHandler"); //$NON-NLS-1$
		field.setFieldIdentifier(fieldIdentifier);
		List<Field> fields = new ArrayList<Field>();
		fields.add(field);
		fieldData.setField(fields);

		DataMap data = new DataMap();
		com.ge.predix.entity.util.map.Map m = new com.ge.predix.entity.util.map.Map();
		m.put("GET_FIELD_RESULT", result); //$NON-NLS-1$
		data.setMap(m);

		fieldData.setData(data);
		criteria.setFieldData(fieldData);
		List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
		list.add(criteria);
		putFieldDataRequest.setPutFieldDataCriteria(list);

		return putFieldDataRequest;
	}

	private PutFieldDataRequest createTimeseriesPutFieldDataRequest(DatapointsIngestion datapointsIngestion) {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		PutFieldDataCriteria criteria = new PutFieldDataCriteria();
		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();

		fieldIdentifier.setSource(FieldSourceEnum.PREDIX_TIMESERIES.toString());
		field.setFieldIdentifier(fieldIdentifier);
		List<Field> fields = new ArrayList<Field>();
		fields.add(field);
		fieldData.setField(fields);
		fieldData.setData(datapointsIngestion);
		criteria.setFieldData(fieldData);
		List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
		list.add(criteria);
		putFieldDataRequest.setPutFieldDataCriteria(list);

		return putFieldDataRequest;
	}
}
