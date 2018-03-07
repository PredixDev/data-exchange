package com.ge.predix.solsvc.fdh.router.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mimosa.osacbmv3_3.DMBool;
import org.mimosa.osacbmv3_3.DMReal;

import com.ge.predix.entity.assetfilter.AssetFilter;
import com.ge.predix.entity.datafile.DataFile;
import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddata.OsaData;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldidentifiervalue.FieldIdentifierValue;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.filter.FieldFilter;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.metadata.MetaData;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.DatapointsQuery;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.Tag;
import com.ge.predix.entity.timeseriesfilter.TimeseriesFilter;
import com.ge.predix.solsvc.ext.util.JsonMapper;

/**
 * 
 * @author predix
 */
public class TestData {

	/**
	 * @param field
	 *            -
	 * @param fieldSource
	 *            -
	 * @param expectedDataType
	 *            -
	 * @param uriField
	 *            -
	 * @param uriFieldValue
	 *            -
	 * @param startTime
	 *            -
	 * @param endTime
	 *            -
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static GetFieldDataRequest getFieldDataRequest(String field, String fieldSource, String expectedDataType,
			Object uriField, Object uriFieldValue, Object startTime, Object endTime) {
		GetFieldDataRequest getFieldDataRequest = new GetFieldDataRequest();
		FieldDataCriteria fieldDataCriteria = new FieldDataCriteria();

		TimeseriesFilter tsFilter = new TimeseriesFilter();
		FieldSelection fieldSelection = new FieldSelection();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId(field);
		fieldIdentifier.setSource(fieldSource);
		fieldSelection.setFieldIdentifier(fieldIdentifier);
		fieldSelection.setExpectedDataType(expectedDataType);
		fieldDataCriteria.getFieldSelection().add(fieldSelection);
		fieldDataCriteria.setFilter(tsFilter);

		// add FieldIdValue pair for assetId
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId(uriField);
		// assetIdFieldIdentifier.setSource(FieldSourceEnum.PREDIX_ASSET.name());
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue(uriFieldValue);

		DatapointsQuery dpQuery = new DatapointsQuery();
		Tag tag1 = new Tag();
		List<Tag> tagList = new ArrayList<Tag>();

		dpQuery.setStart(startTime);
		dpQuery.setEnd(endTime);

		tag1.setName("MYTAG");
		tagList.add(tag1);
		dpQuery.setTags(tagList);
		tsFilter.setDatapointsQuery(dpQuery);

		if (startTime != null && endTime != null) {
			// add FieldIdValue pair for time
			FieldIdentifierValue startTimefieldIdentifierValue = new FieldIdentifierValue();
			FieldIdentifier startTimeFieldIdentifier = new FieldIdentifier();
			startTimeFieldIdentifier.setId("startTime");
			startTimefieldIdentifierValue.setFieldIdentifier(startTimeFieldIdentifier);
			// fieldIdentifierValue.setValue("1438906239475");
			startTimefieldIdentifierValue.setValue(startTime);
			// fieldFilter.getFieldIdentifierValue().add(
			// startTimefieldIdentifierValue);

			FieldIdentifierValue endTimefieldIdentifierValue = new FieldIdentifierValue();
			FieldIdentifier endTimeFieldIdentifier = new FieldIdentifier();
			endTimeFieldIdentifier.setId("endTime");
			endTimefieldIdentifierValue.setFieldIdentifier(endTimeFieldIdentifier);
			// fieldIdentifierValue.setValue("1438906239475");
			endTimefieldIdentifierValue.setValue(endTime);
			// fieldFilter.getFieldIdentifierValue().add(
			// endTimefieldIdentifierValue);
		}

		getFieldDataRequest.getFieldDataCriteria().add(fieldDataCriteria);
		return getFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest putFieldDataRequestSetAlertStatus() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Asset to Query
		AssetFilter filter = new AssetFilter();
		filter.setUri("/asset/compressor-2017");

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier
				.setId("/asset/assetTag/crank-frame-dischargepressure/alertStatusUri/attributes/alertStatus/value");
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		OsaData crankFrameVelocityData = new OsaData();
		DMBool crankFrameVelocity = new DMBool();
		crankFrameVelocity.setValue(true);
		crankFrameVelocityData.setDataEvent(crankFrameVelocity);
		fieldData.getField().add(field);
		fieldData.setData(crankFrameVelocityData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		fieldDataCriteria.setFilter(filter);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest putMetaDataFieldDataRequest() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		MetaData metaData = new MetaData();
		metaData.setSource("handler/assetPutFieldDataHandle");
		metaData.setDescription("putFieldDataRequestSetAlertStatus Meta Data ");
		metaData.setName("Asset Single Put request");
		fieldData.getField().add(field);
		fieldData.setData(metaData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest customPutFieldDataRequest() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Asset to Query
		FieldFilter filter = new FieldFilter();
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId("/asset/assetId");
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue("/asset/compressor-2017");
		filter.getFieldIdentifierValue().add(fieldIdentifierValue);

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId("/asset/assetTag/crank-frame-velocity/hiAlarmThreshold");
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		OsaData crankFrameVelocityData = new OsaData();
		DMReal crankFrameVelocity = new DMReal();
		crankFrameVelocity.setValue(19.88);
		crankFrameVelocityData.setDataEvent(crankFrameVelocity);
		fieldData.getField().add(field);
		fieldData.setData(crankFrameVelocityData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		fieldDataCriteria.setFilter(filter);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest assetPutFieldDataRequest() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Asset to Query
		AssetFilter assetfilter = new AssetFilter();
		assetfilter.setUri("/asset/compressor-2017");

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId("/asset/assetTag/crank-frame-velocity/hiAlarmThreshold");
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		OsaData crankFrameVelocityData = new OsaData();
		DMReal crankFrameVelocity = new DMReal();
		crankFrameVelocity.setValue(19.88);
		crankFrameVelocityData.setDataEvent(crankFrameVelocity);
		fieldData.getField().add(field);
		fieldData.setData(crankFrameVelocityData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		fieldDataCriteria.setFilter(assetfilter);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @param assetId
	 *            -
	 * @param nodeName
	 *            -
	 * @param lowerThreshold
	 *            -
	 * @param upperThreshold
	 *            -
	 * @return -
	 */
	public static PutFieldDataRequest putFieldDataRequest(String assetId, String nodeName, double lowerThreshold,
			double upperThreshold) {
		DatapointsIngestion datapointsIngestion = createTimeseriesDataBody(assetId, nodeName, lowerThreshold,
				upperThreshold);
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		PutFieldDataCriteria criteria = new PutFieldDataCriteria();
		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();

		fieldIdentifier.setSource(FieldSourceEnum.PREDIX_TIMESERIES.name());
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

	@SuppressWarnings("nls")
	private static DatapointsIngestion createTimeseriesDataBody(String assetId, String nodeName, double lowerThreshold,
			double upperThreshold) {
		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(UUID.randomUUID().toString());
		List<Body> bodies = new ArrayList<Body>();
		// log.info("NodeList : " + this.mapper.toJson(aNodeList));

		Body body = new Body();
		List<Object> datapoints = new ArrayList<Object>();
		body.setName(assetId + ":" + nodeName);

		List<Object> datapoint = new ArrayList<Object>();
		datapoint.add(getCurrentTimestamp());
		datapoint.add(generateRandomUsageValue(lowerThreshold, upperThreshold));
		datapoints.add(datapoint);

		body.setDatapoints(datapoints);
		bodies.add(body);

		dpIngestion.setBody(bodies);

		return dpIngestion;
	}

	private static Timestamp getCurrentTimestamp() {
		java.util.Date date = new java.util.Date();
		Timestamp ts = new Timestamp(date.getTime());
		return ts;
	}

	private static double generateRandomUsageValue(double low, double high) {
		return low + Math.random() * (high - low);
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest putMetaDataWithAssetDataFileRequest() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		MetaData metaData = new MetaData();
		metaData.setSource("handler/assetPutFieldDataHandle");
		metaData.setDescription("putFieldDataRequestSetAlertStatus Meta Data ");
		metaData.setName("Asset Data File request");
		fieldData.getField().add(field);
		fieldData.setData(metaData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		PutFieldDataCriteria fieldDataFileCriteria = new PutFieldDataCriteria();
		// Data to change
		fieldData = new FieldData();
		field = new com.ge.predix.entity.field.Field();
		fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		DataFile dataFile = new DataFile();
		dataFile.setName("Asset Test File");
		fieldData.getField().add(field);
		fieldData.setData(dataFile);
		fieldDataFileCriteria.setFieldData(fieldData);

		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataFileCriteria);

		return putFieldDataRequest;
	}

	/***
	 * 
	 * @param dataexUri
	 *            -
	 * @return -
	 */
	public static GetFieldDataRequest getDataExchangeMetaDataRequest(String dataexUri) {
		GetFieldDataRequest getFieldDataRequest = new GetFieldDataRequest();
		FieldDataCriteria fieldDataCriteria = new FieldDataCriteria();
		List<FieldSelection> fieldSelections = new ArrayList<FieldSelection>();
		FieldSelection fieldSelection = new FieldSelection();
		AssetFilter assetFilter = new AssetFilter();

		// add FieldIdValue pair for assetId
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId("MetaData"); //$NON-NLS-1$
		assetIdFieldIdentifier.setSource(FieldSourceEnum.PREDIX_ASSET.name());
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue(dataexUri);
		fieldSelection.setFieldIdentifier(assetIdFieldIdentifier);
		fieldSelections.add(fieldSelection);

		assetFilter.setUri(dataexUri);
		fieldDataCriteria.setFilter(assetFilter);
		fieldDataCriteria.setFieldSelection(fieldSelections);
		getFieldDataRequest.getFieldDataCriteria().add(fieldDataCriteria);
		return getFieldDataRequest;
	}

	/**
	 * @param assetUri
	 *            -
	 * @param selection
	 *            -
	 * @param expectedDataType
	 *            -
	 * @return -
	 */
	public static GetFieldDataRequest createGetAssetRequest(String assetUri, String selection,
			String expectedDataType) {
		GetFieldDataRequest getFieldDataRequest = new GetFieldDataRequest();

		FieldDataCriteria fieldDataCriteria = new FieldDataCriteria();

		// SELECT
		FieldSelection fieldSelection = new FieldSelection();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setSource(FieldSourceEnum.PREDIX_ASSET.name());
		fieldIdentifier.setId(selection);
		fieldSelection.setFieldIdentifier(fieldIdentifier);
		fieldSelection.setExpectedDataType(expectedDataType);

		// SELECT
		fieldDataCriteria.getFieldSelection().add(fieldSelection);

		// FILTER
		AssetFilter assetFilter = new AssetFilter();
		assetFilter.setUri(assetUri);

		// WHERE
		fieldDataCriteria.setFilter(assetFilter);

		getFieldDataRequest.getFieldDataCriteria().add(fieldDataCriteria);
		return getFieldDataRequest;
	}

	/**
	 * @param assetId -
	 * @param nodeName -
	 * @param lowerThreshold -
	 * @param upperThreshold -
	 * @param jsonMapper -
	 * @return -
	 */
	public static PutFieldDataRequest putFieldDataRequestEventHub(String assetId, String nodeName,
			double lowerThreshold, double upperThreshold, JsonMapper jsonMapper) {
		DatapointsIngestion datapointsIngestion = createTimeseriesDataBody(assetId, nodeName, lowerThreshold,
				upperThreshold);
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

		PredixString data = new PredixString();
		data.setString(jsonMapper.toJson(datapointsIngestion));
		fieldData.setData(data);
		criteria.setFieldData(fieldData);
		List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
		list.add(criteria);
		putFieldDataRequest.setPutFieldDataCriteria(list);

		return putFieldDataRequest;
	}
}
