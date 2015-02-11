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
package com.globalsight.everest.webapp.pagehandler.administration.localepairs;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

/**
 * Pagehandler for the new LocalePair page
 */
public class LocaleBasicHandler extends PageHandler
        implements LocalePairConstants
{
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (action.equals(LocalePairConstants.CREATE))
            {
                setValidLocales(session, p_request);
            }
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Set valid locales in the request
     */
    private void setValidLocales(HttpSession p_session, HttpServletRequest p_request)
    throws NamingException, RemoteException, GeneralException
    {        
        Map<String, String> langMap = new HashMap<String, String>();
        Map<String, String> countryMap = new HashMap<String, String>();
        
		String[] langs = Locale.getISOLanguages();
        for (String lang : langs)
        {
            Locale locale = new Locale(lang, "");
            String language = locale.getLanguage();
            language = "in".equals(language) ? "id" : language;
            langMap.put(locale.getDisplayLanguage(Locale.US), language);
        }
		String[] countries = Locale.getISOCountries();
		for (String country : countries) {
			Locale locale = new Locale("", country);
			countryMap.put(locale.getDisplayCountry(Locale.US), locale.getCountry());
		}

        p_request.setAttribute(LocalePairConstants.LANGUAGE, langMap);        
        p_request.setAttribute(LocalePairConstants.COUNTRIES, countryMap);        
    }
}


