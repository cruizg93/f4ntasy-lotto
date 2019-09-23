package com.devteam.fantasy.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.slf4jfilter")
public class LoggingFilterConfiguration {

	public static final String DEFAULT_RESPONSE_TOKEN_HEADER = "Response_Token";
	public static final String DEFAULT_MDC_UUID_TOKEN_KEY = "LoggingFilter.UUID";
	
	private String responseHeader = DEFAULT_RESPONSE_TOKEN_HEADER;
	private String mdcTokenKey = DEFAULT_MDC_UUID_TOKEN_KEY;
	private String requestHeader = null;
    
	@Bean
    public FilterRegistrationBean<LoggingFilter> servletRegistrationBean() {
        final FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        final LoggingFilter log4jMDCFilterFilter = new LoggingFilter(responseHeader, mdcTokenKey, requestHeader);
        registrationBean.setFilter(log4jMDCFilterFilter);
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
