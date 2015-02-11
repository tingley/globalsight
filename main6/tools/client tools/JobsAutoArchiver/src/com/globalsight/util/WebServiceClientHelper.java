/**
 *  Copyright 2014 Welocalize, Inc. 
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

import java.io.File;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.globalsight.www.webservices.Ambassador;
import com.globalsight.www.webservices.AmbassadorServiceLocator;


public class WebServiceClientHelper 
{
    /**
     * Retrieve a proxy to invoke web service APIs of GlobalSight.
     * 
     * As GlobalSight needs IP filter, username/password is required in
     * "IpAddressFilterHandler". Use "HTTP" default rather than "HTTPS".
     * 
     * @param p_hostName
     *            : Host name or IP
     * @param port
     *            : Host port
     * @param userName
     *            : Username to logon
     * @param password
     *            : Password to logon
     * 
     * @return Ambassador proxy
     * 
     * @throws Exception
     */
    public static Ambassador getClientAmbassador(String p_hostName, int port,
            String userName, String password, boolean enableHttps)
            throws Exception
    {
        if (p_hostName == null || "".equals(p_hostName.trim()))
        {
            throw new Exception("Invalid hostName : " + p_hostName);
        }

        if (port < 1)
        {
            throw new Exception("Invalid port : " + port);
        }

        String wsdlUrl = null;
        if (enableHttps)
        {
            wsdlUrl = "https://" + p_hostName + ":" + port
                    + "/globalsight/services/AmbassadorWebService?wsdl";
            setTrustStore();
            // acceptAllCerts();
        }
        else
        {
            wsdlUrl = "http://" + p_hostName + ":" + port
                    + "/globalsight/services/AmbassadorWebService?wsdl";
        }

        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        URL url = new URL(wsdlUrl);
        return loc.getAmbassadorWebService(url, userName, password);
    }
	
    /**
     * Retrieve a proxy to invoke web service APIs of GlobalSight.
     * 
     * As GlobalSight needs IP filter, username/password is required in
     * "IpAddressFilterHandler".
     * 
     * @param p_endpoint
     *            : Wsdl URL
     * @param userName
     *            : Username to logon
     * @param password
     *            : Password to logon
     * 
     * @return Ambassador proxy
     * 
     * @throws Exception
     */
    public static Ambassador getClientAmbassador(String p_endpoint,
            String userName, String password) throws Exception
    {
        if (p_endpoint == null || "".equals(p_endpoint.trim()))
        {
            throw new Exception("Invalid endpoint : " + p_endpoint);
        }

        if (p_endpoint.startsWith("https:"))
        {
            setTrustStore();
            // acceptAllCerts();
        }

        AmbassadorServiceLocator loc = new AmbassadorServiceLocator();
        URL url = new URL(p_endpoint);
        return loc.getAmbassadorWebService(url, userName, password);
    }
	
    /**
     * remove the company name after '+_+' in full token.
     * 
     * @param fullToken
     * @return realToken
     */
    public static String getRealAccessToken(String p_fullAccessToken)
    {
        String realToken = p_fullAccessToken;
        if (p_fullAccessToken != null && !"".equals(p_fullAccessToken.trim()))
        {
            int index = p_fullAccessToken.indexOf("+_+");
            if (index > 0)
            {
                realToken = p_fullAccessToken.substring(0, index);
            }
        }

        return realToken;
    }
	
    public static void acceptAllCerts() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[]
        { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType)
            {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType)
            {
            }
        } };

        // Let's create the factory where we can set some parameters for the
        // connection
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    
    /**
     * Set "javax.net.ssl.trustStore" property.
     * 
     * Note that "JAVA_HOME" must be configured on server, and certification
     * authorization must be setup. Commonly "cacerts" and "jssecacerts" files
     * are default CA files, so check the two files and add it as
     * "trustStore"("cacerts" file has higher priority than "jssecacerts" in
     * GloalSight).
     * 
     */
    private static void setTrustStore()
    {
        StringBuffer certsPath = new StringBuffer();

        String javaHome = System.getProperty("java.home");
        certsPath.append(javaHome == null ? "" : javaHome)
                .append(File.separator).append("lib").append(File.separator)
                .append("security");

        File file = new File(certsPath.toString(), "cacerts");
        if (!file.exists() || !file.isFile())
        {
            file = new File(certsPath.toString(), "jssecacerts");
        }

        if (file.exists())
        {
            String path = file.getAbsolutePath();
            System.setProperty("javax.net.ssl.trustStore", path);
            // password is not required
            // System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        }

    }
}
