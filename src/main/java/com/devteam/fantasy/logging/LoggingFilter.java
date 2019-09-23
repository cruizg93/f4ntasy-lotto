package com.devteam.fantasy.logging;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoggingFilter extends OncePerRequestFilter{
	
	private final String responseHeader;
    private final String mdcTokenKey;
    private final String requestHeader;
    
    public LoggingFilter() {
        responseHeader = LoggingFilterConfiguration.DEFAULT_RESPONSE_TOKEN_HEADER;
        mdcTokenKey = LoggingFilterConfiguration.DEFAULT_MDC_UUID_TOKEN_KEY;
        requestHeader = null;
    }
    
	public LoggingFilter(final String responseHeader, final String mdcTokenKey, final String requestHeader) {
		super();
		this.responseHeader = responseHeader;
		this.mdcTokenKey = mdcTokenKey;
		this.requestHeader = requestHeader;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
			throws ServletException, IOException {
		try {
            final String token;
            if (!StringUtils.isEmpty(requestHeader) && !StringUtils.isEmpty(request.getHeader(requestHeader))) {
                token = request.getHeader(requestHeader);
            } else {
                token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
            }
            MDC.put(mdcTokenKey, token);
            
            if (!StringUtils.isEmpty(responseHeader)) {
                response.addHeader(responseHeader, token);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcTokenKey);
        }
	}

	public String getResponseHeader() {
		return responseHeader;
	}

	public String getMdcTokenKey() {
		return mdcTokenKey;
	}

	public String getRequestHeader() {
		return requestHeader;
	}

	@Override
	public String toString() {
		return "LoggingFilter [responseHeader=" + responseHeader + ", mdcTokenKey=" + mdcTokenKey + ", requestHeader="
				+ requestHeader + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mdcTokenKey == null) ? 0 : mdcTokenKey.hashCode());
		result = prime * result + ((requestHeader == null) ? 0 : requestHeader.hashCode());
		result = prime * result + ((responseHeader == null) ? 0 : responseHeader.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoggingFilter other = (LoggingFilter) obj;
		if (mdcTokenKey == null) {
			if (other.mdcTokenKey != null)
				return false;
		} else if (!mdcTokenKey.equals(other.mdcTokenKey))
			return false;
		if (requestHeader == null) {
			if (other.requestHeader != null)
				return false;
		} else if (!requestHeader.equals(other.requestHeader))
			return false;
		if (responseHeader == null) {
			if (other.responseHeader != null)
				return false;
		} else if (!responseHeader.equals(other.responseHeader))
			return false;
		return true;
	}
	
}
