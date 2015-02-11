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
package com.globalsight.everest.webapp.applet.util;

import java.net.URL;
import java.util.StringTokenizer;

import com.sun.java.browser.net.ProxyInfo;
import com.sun.java.browser.net.ProxyService;

/**
 * A class that provides ProxyInfo and means of detection Proxy info.
 */
public class GlobalSightProxyInfo implements ProxyInfo
{
    private int m_port = 0;

    private boolean m_isSocks = false;

    private String m_host = "";

    /**
     * Creates a GlobalSightProxyInfo
     * 
     * @param p_port
     * @param p_isSocks
     * @param p_host
     */
    public GlobalSightProxyInfo(int p_port, boolean p_isSocks, String p_host)
    {
        m_port = p_port;
        m_isSocks = p_isSocks;
        m_host = p_host;
    }

    /**
     * Creates a GlobalSightProxyInfo with information from the given ProxyInfo
     * 
     * @param p_proxyInfo
     *            some other ProxyInfo
     */
    public GlobalSightProxyInfo(ProxyInfo p_proxyInfo)
    {
        this(p_proxyInfo.getPort(), p_proxyInfo.isSocks(), p_proxyInfo
                .getHost());
    }

    /**
     * Returns the proxy port
     * 
     * @return int
     */
    public int getPort()
    {
        return m_port;
    }

    /**
     * Returns whether this proxy is using socks
     * 
     * @return
     */
    public boolean isSocks()
    {
        return m_isSocks;
    }

    /**
     * Returns the name of those proxy host
     * 
     * @return String
     */
    public java.lang.String getHost()
    {
        return m_host;
    }

    /**
     * Tries to detect the proxy settings from the web browser. It will attempt
     * to lookup the URL and see what kind of proxy would be needed for it. This
     * uses the undocumented sun proxy classes.
     * 
     * @param p_url
     *            some URL
     * @return ProxyInfo
     */
    public static ProxyInfo detectProxyInfoFromBrowser(URL p_url)
    {
        System.out.println("---- Detecting proxy settings from web browser.");
        ProxyInfo proxyInfo = null;
        try
        {
            System.out.println("---- using URL " + p_url);
            ProxyInfo infos[] = ProxyService.getProxyInfo(p_url);
            if (infos != null && infos.length > 0)
            {
                proxyInfo = infos[0];
                System.out.println("---- Found proxy setting: "
                        + proxyInfo.getHost() + "," + proxyInfo.getPort() + ","
                        + proxyInfo.isSocks());
            }
            else
            {
                System.out
                        .println("---- No proxy info from the proxy service.");
            }
        }
        catch (Exception ex)
        {
            System.out.println("---- Could not retrieve proxy configuration: "
                    + ex.getMessage());
        }

        return proxyInfo;
    }

    /**
     * Tries to detect the proxy settings from the system by reading the System
     * property javaplugin.proxy.config.list. NOTE: This works fine for
     * detecting manual proxy setings, and automatic in some cases, but not when
     * automatic script configuration is used in IE.
     * 
     * @return ProxyInfo
     */
    private ProxyInfo detectProxyInfoFromSystem()
    {
        GlobalSightProxyInfo myProxyInfo = null;
        String proxyList = System.getProperty("javaplugin.proxy.config.list");
        String proxyHost = "";
        int proxyPort = -1;
        boolean hasProxy = false;
        System.out
                .println("---- Detecting proxy settings from system. ProxyList="
                        + proxyList);
        if (proxyList != null && !"".equals(proxyList))
        {
            StringTokenizer tokenizer1 = new StringTokenizer(proxyList, ";,");
            if (tokenizer1.countTokens() > 1)
            {
                while (tokenizer1.hasMoreTokens())
                {
                    String s = tokenizer1.nextToken();
                    if (s.toLowerCase().startsWith("http"))
                    {
                        StringTokenizer tokenizer2 = new StringTokenizer(s,
                                "=:");
                        tokenizer2.nextToken();
                        proxyHost = tokenizer2.nextToken();
                        proxyPort = Integer.parseInt(tokenizer2.nextToken());
                        hasProxy = true;
                    }
                }
            }
            else
            {
                StringTokenizer tokenizer = new StringTokenizer(proxyList, ":");
                proxyHost = tokenizer.nextToken();
                proxyPort = Integer.parseInt(tokenizer.nextToken());
                hasProxy = true;
            }
        }

        if (hasProxy)
        {
            // since all we know is the proxy name and port, assume socks is
            // false
            myProxyInfo = new GlobalSightProxyInfo(proxyPort, false, proxyHost);
            System.out.println("---- Found proxy setting: "
                    + myProxyInfo.getHost() + "," + myProxyInfo.getPort());
        }
        else
        {
            System.out.println("---- No proxy info from system.");
        }

        return myProxyInfo;
    }
}
