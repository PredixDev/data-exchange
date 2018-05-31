package com.ge.predix.solsvc.fdh.router.service.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.util.map.AttributeMap;
import com.ge.predix.entity.util.map.Entry;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.router.config.DXWebSocketServerConfig;

/**
 * 
 * @author predix.adoption@ge.com -
 */
@ServerEndpoint(value = "/livestream/{nodeId}",configurator=DXWebSocketServerConfig.class)
public class DXWebSocketServerEndPoint{
	
	private static Logger log = LoggerFactory.getLogger(DXWebSocketServerEndPoint.class);

	private static final LinkedList<Session> clients = new LinkedList<Session>();
	
	@Autowired
	@Qualifier("putFieldDataService")
	private PutDataRouterImpl putFieldDataService;
	
	private JsonMapper mapper;
	
	private DXWebSocketServerConfig config;
	
	private String nodeId;
	
	private ApplicationContext context;
	
	
	/**
	 * @param nodeId1 - nodeId for the session
	 * @param session - session object
	 * @param ec
	 *            -
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "nodeId") String nodeId1, final Session session, EndpointConfig ec) {
		//logger.info("headers : "+request.getHeaders()); //$NON-NLS-1$
		this.nodeId = nodeId1;
		clients.add(session);
		log.info("Server: opened... for Node Id : " + nodeId1 + " : " + session.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		log.info("Nunmber of open connections : " + session.getOpenSessions().size()); //$NON-NLS-1$
	}

	/**
	 * @param message -
	 * @param session -
	 */
	@SuppressWarnings({ "unchecked", "nls" })
	@OnMessage
	public void onMessage(String message, Session session) {
	    log.info("Node Id : "+session.getPathParameters().get("nodeId")+"Websocket Message : " + message); //$NON-NLS-1$
		String currentNodeId = session.getPathParameters().get("nodeId");
	    //log.debug("RequestParameterMap : "+session.getUserProperties()); //$NON-NLS-1$
		this.context = (ApplicationContext) session.getUserProperties().get("applicationContext");
		this.putFieldDataService = (PutDataRouterImpl) this.context.getBean("putFieldDataService");	
		this.config = this.context.getBean(DXWebSocketServerConfig.class);
		this.mapper = this.context.getBean(JsonMapper.class);
		this.mapper.init();
		PutFieldDataRequest putFieldDataRequest = null;
		DatapointsIngestion timeSeriesRequest = null;
		try {
			if (checkJsonCompatibility(message, PutFieldDataRequest.class)) {
			    log.info("Input message is "+PutFieldDataRequest.class);
			    putFieldDataRequest =  this.mapper.fromJson(message, PutFieldDataRequest.class);
			}else if (checkJsonCompatibility(message, DatapointsIngestion.class)) {
			    log.info("Input message is "+DatapointsIngestion.class);
			    timeSeriesRequest =  this.mapper.fromJson(message, DatapointsIngestion.class);
			}else if ("broadcast".equalsIgnoreCase(currentNodeId)) {
			    log.debug("No of opensessions : " + clients.size()); //$NON-NLS-1$
                for (Session s:clients) {
                    if (s.isOpen() && !this.nodeId.equals(s.getPathParameters().get("nodeId"))) {
                        s.getBasicRemote().sendText(message);
                    }
                }
                session.getBasicRemote().sendText("SUCCESS"); //$NON-NLS-1$
            }
			if ("messages".equalsIgnoreCase(currentNodeId)) { //$NON-NLS-1$
				if (timeSeriesRequest != null && timeSeriesRequest.getMessageId() != null) {
					putFieldDataRequest = new PutFieldDataRequest();
					putFieldDataRequest.setCorrelationId(UUID.randomUUID().toString());
					PutFieldDataCriteria criteria = new PutFieldDataCriteria();
					FieldData fieldData = new FieldData();
					String[] fieldSources = this.config.getDefaultFieldSource().split(","); 
					for (String source:fieldSources) {
						Field field = new Field();
						FieldIdentifier  fieldIdentifier = new FieldIdentifier();
						fieldIdentifier.setSource(source);
						field.setFieldIdentifier(fieldIdentifier);
						fieldData.getField().add(field);
					}
					Field field = new Field();
					FieldIdentifier  fieldIdentifier = new FieldIdentifier();
					fieldIdentifier.setSource("handler/webSocketHandler");
					field.setFieldIdentifier(fieldIdentifier);
					fieldData.getField().add(field);
                    
					fieldData.setData(timeSeriesRequest);
					criteria.setFieldData(fieldData);
					List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
					list.add(criteria);
					putFieldDataRequest.setPutFieldDataCriteria(list);
				}
				log.info("PutFieldDataRequest "+this.mapper.toJson(putFieldDataRequest));
				if (putFieldDataRequest != null) {
					//TODO support headers passed in
				    List<Header> headers = new ArrayList<Header>();
	                String[] headerNames = {"authorization","predix-zone-id"};
	                //log.debug(session.getUserProperties().toString());
	                Map<String,List<String>> headerMap = (Map<String, List<String>>) session.getUserProperties().get("headers");
	                
	                for (String headerName:headerNames) {
	                    log.debug("Header Name "+headerName);
	                    List<String> values = headerMap.get(headerName);
	                    if (values != null) {
	                        headers.add(new BasicHeader(headerName, values.get(0)));
	                    }
	                }
	                AttributeMap attributeMap = new AttributeMap();
	                Entry entryClients = new Entry();
	                entryClients.setKey("SESSIONS");
	                entryClients.setValue(clients);
	                attributeMap.getEntry().add(entryClients);
	                
	                Entry entryCurrentSession = new Entry();
	                entryCurrentSession.setKey("SESSION");
	                entryCurrentSession.setValue(session);
	                attributeMap.getEntry().add(entryCurrentSession);
	                
	                
	                putFieldDataRequest.setExternalAttributeMap(attributeMap);
	                
	                this.putFieldDataService.putData(putFieldDataRequest, null, headers);
	                log.debug("No of opensessions : " + clients.size()); //$NON-NLS-1$
	                for (Session s:clients) {
	                    if (s.isOpen() && !this.nodeId.equals(currentNodeId)) {
	                        s.getBasicRemote().sendText(message);
	                    }
	                }
	                String response = "{\"messageId\": " + putFieldDataRequest.getCorrelationId() + ",\"statusCode\": 202}"; //$NON-NLS-1$ //$NON-NLS-2$ 
	                session.getBasicRemote().sendText(response);
				}
				
			}			
		} catch (Throwable e) {
		    log.error("unable to process websocket message",e);
		    throw new RuntimeException("unable to process websocket message",e);
		}
	}

	
	/**
	 * @param session
	 *            - session object
	 * @param closeReason
	 *            - The reason of close of session
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
	    log.info("Server: Session " + session.getId() + " closed because of " + closeReason.getReasonPhrase()); //$NON-NLS-1$ //$NON-NLS-2$
		clients.remove(session);
	}

	/**
	 * @param session
	 *            - current session object
	 * @param t
	 *            - Throwable instance containing error info
	 */
	@OnError
	public void onError(Session session, Throwable t) {
		log.error("Exception occured ",t); //$NON-NLS-1$ 
	}
	
	private boolean checkJsonCompatibility(String jsonStr, Class<?> valueType) throws JsonParseException, IOException {

        ObjectMapper jsonmapper = new ObjectMapper();

        try {
            jsonmapper.readValue(jsonStr, valueType);
            return true;
        } catch (JsonMappingException e) {
            return false;
        } 

    }
}
