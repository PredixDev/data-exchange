package com.ge.predix.solsvc.fdh.router.boot;

import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * DataEchangeRouter routes traffic to FDH Handlers. FDH Handlers implement the exact
 * same API Interface and can be called directly. But this microservice provides
 * a single point of entry for Analytic Service calls and then the calls fan out
 * based on rules.
 * 
 * @author predix
 */
@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "com.ge.predix.solsvc.fdh.handler")
@EnableAutoConfiguration(exclude =
{
        DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,
        PersistenceExceptionTranslationAutoConfiguration.class
})
@ImportResource(
{
        "classpath*:META-INF/spring/data-exchange-cxf-context.xml",
        "classpath*:META-INF/spring/data-exchange-scan-context.xml"
})
@PropertySource("classpath:application-default.properties")
@EnableWebSocket
public class DataExchangeRouterApplication extends PredixSpringBootInitializer
{

    private static final Logger log = LoggerFactory.getLogger(DataExchangeRouterApplication.class);

    /**
     * @param args
     *            -
     */
    public static void main(String[] args)
    {
        Logger log = LoggerFactory.getLogger(DataExchangeRouterApplication.class);

        SpringApplication springApplication = new SpringApplication(DataExchangeRouterApplication.class);
        ApplicationContext ctx = springApplication.run(args);

        log.info("Let's inspect the beans provided by Spring Boot:"); //$NON-NLS-1$

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames)
        {
            log.info(beanName);
        }

        // log.info("Let's inspect the profiles provided by Spring Boot:");
        String profiles[] = ctx.getEnvironment().getActiveProfiles();
        for (int i = 0; i < profiles.length; i++)
            log.info("profile=" + profiles[i]); //$NON-NLS-1$

        log.info("Let's inspect the properties provided by Spring Boot:"); //$NON-NLS-1$
        MutablePropertySources propertySources = ((StandardServletEnvironment) ctx.getEnvironment())
                .getPropertySources();
        Iterator<org.springframework.core.env.PropertySource<?>> iterator = propertySources.iterator();
        while (iterator.hasNext())
        {
            Object propertySourceObject = iterator.next();
            if ( propertySourceObject instanceof org.springframework.core.env.PropertySource )
            {
                org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) propertySourceObject;
                log.info("propertySource=" + propertySource.getName() + " values=" + propertySource.getSource() //$NON-NLS-1$ //$NON-NLS-2$
                        + "class=" + propertySource.getClass()); //$NON-NLS-1$
            }
        }

        // Spring is a little bit of a hog on startup, after that it's not too bad.
        System.gc();
        printMemory();

    }

    /**
     * -
     */
    @SuppressWarnings("nls")
    public static void printMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024 * 1024;

        log.info("##### Heap utilization statistics [MB] #####");

        // Print used memory
        log.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        log.info("Free Memory:" + runtime.freeMemory() / mb);

        // Print total available memory
        log.info("Total Memory:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        log.info("Max Memory:" + runtime.maxMemory() / mb);

        // log.info("VCAP_SERVICES"+System.getenv("VCAP_SERVICES"));
    }

    /**
     * Add this bean or the @PropertySource above won't kick in
     * 
     * @return -
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
