/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class HttpHeaderSecurityFilter implements Filter
{
    // HSTS
    private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
    private boolean hstsEnabled = true;
    private int hstsMaxAgeSeconds = 31536000;
    private boolean hstsIncludeSubDomains = true;
    private String hstsHeaderValue;

    // Click-jacking protection
    private static final String ANTI_CLICK_JACKING_HEADER_NAME = "X-Frame-Options";
    private boolean antiClickJackingEnabled = true;
    private String antiClickJackingOption = "SAMEORIGIN";
    private String antiClickJackingHeaderValue;

    // Block content sniffing
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME = "X-Content-Type-Options";
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE = "nosniff";
    private boolean blockContentTypeSniffingEnabled = true;

    public void destroy()
    {

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        if (response.isCommitted())
        {
            return;
        }

        // HSTS
        if (hstsEnabled && request.isSecure()
                && response instanceof HttpServletResponse)
        {
            ((HttpServletResponse) response).setHeader(HSTS_HEADER_NAME,
                    hstsHeaderValue);
        }

        // anti click-jacking
        if (antiClickJackingEnabled && response instanceof HttpServletResponse)
        {
            ((HttpServletResponse) response).setHeader(
                    ANTI_CLICK_JACKING_HEADER_NAME,
                    antiClickJackingHeaderValue);
        }

        // Block content type sniffing
        if (blockContentTypeSniffingEnabled
                && response instanceof HttpServletResponse)
        {
            ((HttpServletResponse) response).setHeader(
                    BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME,
                    BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE);
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {

        // Build HSTS header value
        StringBuilder hstsValue = new StringBuilder("max-age=");
        hstsValue.append(hstsMaxAgeSeconds);
        if (hstsIncludeSubDomains)
        {
            hstsValue.append(";includeSubDomains");
        }
        hstsHeaderValue = hstsValue.toString();

        // Anti click-jacking
        StringBuilder cjValue = new StringBuilder(antiClickJackingOption);
        antiClickJackingHeaderValue = cjValue.toString();

    }
}
