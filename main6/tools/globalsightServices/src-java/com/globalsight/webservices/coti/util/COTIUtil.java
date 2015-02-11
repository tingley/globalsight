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

package com.globalsight.webservices.coti.util;

import java.util.HashMap;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisError;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;

/**
 * UTIL class for COTI service
 * @author Wayzou
 *
 */
public class COTIUtil
{
    public static String GSNameSpace = "http://webservices.globalsight.com";
    public static String COTIsessionId = "COTIsessionId";

    private static String sessionKey = "sessionKey";
    private static String sessionErrorMsg = "Authorization failed: COTIsessionId is not accepted.";
    
    private static Object locker = new Object(); 

    /**
     * Get COTI session by its context
     * @param msgCtx
     * @return null if not login
     */
    public static COTISession getSession(MessageContext msgCtx)
    {
        ConfigurationContext cc = msgCtx.getConfigurationContext();
        Object session = cc.getProperty(sessionKey);
        if (session == null)
        {
            return null;
        }

        HashMap<String, COTISession> sessionMap = (HashMap<String, COTISession>) session;

        SOAPEnvelope e = msgCtx.getEnvelope();
        OMElement ome = e.getHeader().getFirstChildWithName(
                new QName(GSNameSpace, COTIsessionId));

        if (ome == null)
        {
            return null;
        }

        String sid = ome.getText();
        if (sid == null)
        {
            return null;
        }

        if (sessionMap.containsKey(sid))
        {
            COTISession cs = sessionMap.get(sid);
            return cs;
        }

        return null;
    }

    /**
     * Check if this session exists
     * @param msgCtx
     * @throws AxisError if not login
     */
    public static void checkSession(MessageContext msgCtx) throws AxisError
    {
        ConfigurationContext cc = msgCtx.getConfigurationContext();
        Object session = cc.getProperty(sessionKey);
        if (session == null)
        {
            throw new AxisError(sessionErrorMsg);
        }

        HashMap<String, COTISession> sessionMap = (HashMap<String, COTISession>) session;

        SOAPEnvelope e = msgCtx.getEnvelope();
        OMElement ome = e.getHeader().getFirstChildWithName(
                new QName(GSNameSpace, COTIsessionId));

        if (ome == null)
        {
            throw new AxisError(sessionErrorMsg);
        }

        String sid = ome.getText();
        if (sid == null)
        {
            throw new AxisError(sessionErrorMsg);
        }

        sid = sid.trim();

        if (!sessionMap.containsKey(sid))
        {
            throw new AxisError(sessionErrorMsg);
        }
    }

    /**
     * Save one session if login success
     * @param msgCtx
     * @return
     */
    public static String loginSession(MessageContext msgCtx)
    {
        ConfigurationContext cc = msgCtx.getConfigurationContext();
        HashMap<String, COTISession> sessionMap = null;
        
        synchronized (locker)
        {
            Object session = cc.getProperty(sessionKey);
            if (session == null)
            {
                sessionMap = new HashMap<String, COTISession>();
                addDefaultSession(sessionMap);
            }
            else
            {
                sessionMap = (HashMap<String, COTISession>) session;
            }
        }

        SOAPEnvelope e = msgCtx.getEnvelope();
        OMElement ome = e.getHeader();
        String username = "";
        try
        {
            username = ((OMElement) ((OMElement) ((OMElement) ome
                    .getChildrenWithLocalName("Security").next())
                    .getChildrenWithLocalName("UsernameToken").next())
                    .getChildrenWithLocalName("Username").next()).getText();
        }
        catch (Exception ex)
        {
            username = "";
        }

        User u = UserHandlerHelper.getUser(username);
        String companyName = u.getCompanyName();
        String sid = username + "_" + UUID.randomUUID();
        COTISession cs = new COTISession();
        cs.setCompanyName(companyName);
        cs.setSessionId(sid);
        cs.setUserName(username);
        sessionMap.put(sid, cs);

        msgCtx.getConfigurationContext().setProperty(sessionKey, sessionMap);

        return sid;
    }

    /**
     * Logout one session
     * @param msgCtx
     */
    public static void logoutSession(MessageContext msgCtx)
    {
        ConfigurationContext cc = msgCtx.getConfigurationContext();
        Object session = cc.getProperty(sessionKey);
        if (session == null)
        {
            return;
        }

        HashMap<String, COTISession> sessionMap = (HashMap<String, COTISession>) session;

        SOAPEnvelope e = msgCtx.getEnvelope();
        OMElement ome = e.getHeader().getFirstChildWithName(
                new QName(GSNameSpace, COTIsessionId));

        if (ome == null)
        {
            return;
        }

        String sid = ome.getText();
        if (sid == null)
        {
            return;
        }

        if (sessionMap.containsKey(sid))
        {
            COTISession cs = sessionMap.get(sid);
            cs.clear();

            sessionMap.remove(sid);
        }
    }

    /**
     * Add testing session id for local machine
     * @param sessionMap
     */
    private static void addDefaultSession(
            HashMap<String, COTISession> sessionMap)
    {
        String v = System.getProperty("user.name");
        if (v == null || !v.contains("Wayzou"))
        {
            return;
        }
        
        String defaultSessionId = "default_coti_session_id_globalSight";
        COTISession defaultSession = new COTISession();
        defaultSession.setCompanyName("way");
        defaultSession.setSessionId(defaultSessionId);
        defaultSession.setUserName("wayAdmin");
        
        sessionMap.put(defaultSessionId, defaultSession);
    }
}
