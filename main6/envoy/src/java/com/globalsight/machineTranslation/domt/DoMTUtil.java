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
package com.globalsight.machineTranslation.domt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.util.StringUtil;

public class DoMTUtil
{
    private static final Logger logger = Logger.getLogger(DoMTUtil.class);

    public static final String JOB_STATUS_RUNNING = "running";
    public static final String JOB_STATUS_QUEUED = "queued";
    public static final String JOB_STATUS_COMPLETED = "completed";
    public static final String JOB_STATUS_STOPPED = "stopped";
    public static final String JOB_STATUS_FAILED = "failed";
    
    public static final String KEY_STATUS = "status";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_SERVER = "server";
    public static final String KEY_SUBSTATUS = "substatus";
    public static final String KEY_GRAPHERRORS = "grapherrors";

    /**
     * Test if the url and engine name are correct via "run" API.
     * <p>
     * If the url is invalid, the exception message will be
     * "HTTP server returned unexpected status: Not Found"; If the engine name
     * is invalid, the exception message will be "list index out of range".
     * </p>
     * 
     * @param url
     * @param engineName
     * @throws Exception
     */
    public static void testDoMtHost(String url, String engineName)
            throws Exception
    {
        try
        {
            XmlRpcClient client = getXmlRpcClient(url);

            String[] xliff = new String[] { getSampleXlf(engineName) };
            Object[] params = new Object[]{ xliff };
            Object[] returning = (Object[]) client.execute("run", params);

            if (MTHelper.isLogDetailedInfo(DoMTProxy.ENGINE_DOMT))
            {
                logRunInfo(returning);
            }
        }
        catch (Exception e)
        {
            String msg = "Invalid DoMT URL or engine name.";
            logger.warn("Invalid DoMT URL or engine name(" + url + " :: "
                    + engineName + ").");
            logger.warn("Excetion message is :: " + e.getMessage());

            throw new Exception(msg);
        }
    }

    public static XmlRpcClient getXmlRpcClient(String url)
            throws MalformedURLException
    {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        config.setEnabledForExtensions(true);
//        config.setConnectionTimeout(60 * 1000);
//        config.setReplyTimeout(60 * 1000);
        config.setGzipCompressing(true);

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);

        return client;
    }

    /**
     * Get a sample xliff to test host. Here it does not care "source-language"
     * and "target-language".
     * 
     * "translate-xliff" is a default engine on DoMT server, its languages are
     * from "nl" to "en".
     * 
     * @param engineName
     * @return String
     */
    private static String getSampleXlf(String engineName)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        sb.append("<xliff version=\"1.2\">\r\n");
        sb.append("<file original=\"sample-domt\" source-language=\"nl\" target-language=\"en\" datatype=\"multi-format\">\r\n");
        sb.append("<header>\r\n");
        sb.append("<note from=\"PTTOOLS\">\r\n");
        sb.append("    <graphname>").append(engineName).append("</graphname>\r\n");
        sb.append("</note>\r\n");
        sb.append("</header>\r\n");
        sb.append("<body>\r\n");
        sb.append("  <trans-unit id=\"37323\" translate=\"yes\">\r\n");
        sb.append("  <source>Wijziging van de uitvoeringsvoorschriften van het Financieel Reglement</source>\r\n");
        sb.append("   </trans-unit>\r\n");
        sb.append("</body>\r\n");
        sb.append("</file>\r\n");
        sb.append("</xliff>");

        return sb.toString();
    }

    /**
     * Check the returning of "run" API to see if the invoking is successful.
     * 
     * @param returning
     * @return boolean
     */
    public static boolean isRunSucceed(Object[] returning)
    {
        if (returning == null || returning.length < 2)
            return false;

        boolean isRunSucceed = false;

        String jobId = (String) returning[0];
        Object[] errors = (Object[]) returning[1];
        if (StringUtil.isNotEmpty(jobId) && errors.length == 0)
        {
            isRunSucceed = true;
        }

        return isRunSucceed;
    }

    /**
     * Log returned information from "run" API.
     * 
     * @param returning
     *            -- "run" API's returning
     */
    public static void logRunInfo(Object[] returning)
    {
        if (returning == null || returning.length < 2)
        {
            return;
        }
        try
        {
            String jobId = (String) returning[0];
            Object[] errors = (Object[]) returning[1];

            logger.info("jobID :: " + jobId);
            if (errors.length == 0)
            {
                logger.info("Invoke 'run()' succeed!");
            }
            else
            {
                Object[] errors2 = (Object[]) errors[0];
                for (int i = 0; i < errors2.length; i++)
                {
                    logger.info("errors[" + i + "] ::" + (String) errors2[i]);
                }
            }
        }
        catch (Exception ignore)
        {
            logger.warn("Ignored info :: fail to log 'run' info "
                    + ignore.getMessage());
        }
    }

    /**
     * Log returned information from "status" API.
     * 
     * @param status
     *            -- "status" API's returning
     * @param jobId
     *            -- DoMT returned job id.
     */
    @SuppressWarnings("rawtypes")
    public static void logStatusInfo(HashMap status, String jobId)
    {
        if (status == null || jobId == null)
            return;

        try
        {
            logger.info("========== STATUS INFO FOR CURRENT JOB_ID ==========");
            HashMap jobValues = (HashMap) status.get(jobId);
            if (jobValues != null)
            {
                Iterator it = jobValues.entrySet().iterator();
                while (it.hasNext())
                {
                    Entry entry = (Entry) it.next();
                    String key = (String) entry.getKey();
                    Object value = (Object) entry.getValue();
                    if (KEY_CONTENT.equalsIgnoreCase(key))
                    {
                        Object[] content = (Object[]) value;
                        if (content.length > 0)
                        {
//                            logger.info("content :: " + content[0]);
                        }
                        else
                        {
                            logger.info("'content' is empty");
                        }
                    }
                    else if (KEY_GRAPHERRORS.equalsIgnoreCase(key))
                    {
                        Object[] grapherrors = (Object[]) value;
                        if (grapherrors.length > 0)
                        {
                            logger.info(key + " :: " + grapherrors[0]);                            
                        }
                    }
                    else
                    {
                        logger.info(key + " :: " + value.toString());
                    }
                }
            }

            logger.info("========== STATUS INFO FOR CURRENT SERVER ==========");
            HashMap serverValues = (HashMap) status.get(KEY_SERVER);
            if (serverValues != null)
            {
                Iterator it2 = serverValues.entrySet().iterator();
                while (it2.hasNext())
                {
                    Entry entry = (Entry) it2.next();
                    String key = (String) entry.getKey();
                    Object value = (Object) entry.getValue();

                    if (JOB_STATUS_QUEUED.equalsIgnoreCase(key)
                            || JOB_STATUS_STOPPED.equalsIgnoreCase(key)
                            || JOB_STATUS_FAILED.equalsIgnoreCase(key)
                            || JOB_STATUS_COMPLETED.equalsIgnoreCase(key)
                            || JOB_STATUS_RUNNING.equalsIgnoreCase(key))
                    {
                        Object[] jobStatus = (Object[]) value;
                        if (jobStatus.length > 0)
                        {
                            logger.info(key + " :: " + jobStatus[0]);
                        }
                        else
                        {
                            logger.info("'" + key + "' job list is empty!");
                        }
                    }
                    else if ("substatus".equalsIgnoreCase(key))
                    {
                        // ignore this as it is confused.
                    }
                    else
                    {
                        logger.info(key + " :: " + value.toString());
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            logger.warn("Ignored info :: fail to log status info "
                    + ignore.getMessage());
        }
    }

    /**
     * GlobalSight sends "lang_country" style to DoMT in lower-case.
     */
    public static String checkLang(String lang, String country)
    {
        return (lang + "_" + country).toLowerCase();
    }
}
