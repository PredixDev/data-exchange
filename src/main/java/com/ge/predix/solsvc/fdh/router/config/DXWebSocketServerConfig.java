package com.ge.predix.solsvc.fdh.router.config;

import javax.servlet.ServletContext;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.ge.predix.solsvc.fdh.router.service.router.DXWebSocketServerEndPoint;

/**
 * 
 * @author predix.adoption@ge.com -
 */
@Configuration
public class DXWebSocketServerConfig extends ServerEndpointConfig.Configurator implements ApplicationContextAware{
	@Value("${predix.dataexchange.default.fieldsource:PREDIX_EVENT_HUB}")
    private String                 defaultFieldSource;
	
	/* (non-Javadoc)
	 * @see javax.websocket.server.ServerEndpointConfig.Configurator#modifyHandshake(javax.websocket.server.ServerEndpointConfig, javax.websocket.server.HandshakeRequest, javax.websocket.HandshakeResponse)
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		sec.getUserProperties().put("headers", request.getHeaders()); //$NON-NLS-1$
		sec.getUserProperties().put("applicationContext", DXWebSocketServerConfig.context); //$NON-NLS-1$
		super.modifyHandshake(sec, request, response);
	}
	
	/**
     * Spring application context.
     */
    private static volatile ApplicationContext context;

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return context.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    /**
     * @return -
     */
    @Profile("cloud")
    @Bean       
    public ServerEndpointExporter serverEndpointExporter() {        
          return new ServerEndpointExporter();        
    }

    /**
     * @param applicationContext - 
     * @return -
     */
    @Profile("local")
    @Bean
    public ServletContextAware endpointExporterInitializer(final ApplicationContext applicationContext) {
        return new ServletContextAware() {

            @Override
            public void setServletContext(ServletContext servletContext) {
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter();
                    serverEndpointExporter.setApplicationContext(applicationContext);
                try {
                    serverEndpointExporter.afterPropertiesSet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }               
            }           
        };
    }
	/**
	 * @return -
	 */
	public String getDefaultFieldSource() {
		return this.defaultFieldSource;
	}

	/**
	 * @param defaultFieldSource -
	 */
	public void setDefaultFieldSource(String defaultFieldSource) {
		this.defaultFieldSource = defaultFieldSource;
	}
	
	/**
	 * @return -
	 */
	@Bean
    public DXWebSocketServerEndPoint dXWebSocketServerEndPoint() {
        return new DXWebSocketServerEndPoint();
    }
}
