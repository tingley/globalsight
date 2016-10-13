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
package com.globalsight.cxe.servlet.imagereplace;

/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

//  Java
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.util.Properties;
import java.util.MissingResourceException;

//  Java Extensions
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

//  SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//  GlobalSight
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.diplomat.util.Logger;

/**
 * This is the servlet for exporting images back to the target file system or
 * CMS system. This is the main entry point for exporting images. Based on the
 * target system, the request is forwarded on to appropriate image handler.
 * <p>
 * If there is no image replacement handler defined then it sets the
 * status for the request to SC_NOT_IMPLEMENTED.
 *
 */
public class ImageReplace extends HttpServlet
{
    private Logger theLogger = Logger.getLogger();
    private Properties m_props = new Properties();

    public ImageReplace()
    throws ServletException
    {
        try
        {
            theLogger.setLogname("CxeServlets");
            InputStream is = ImageReplace.class.getResourceAsStream("/properties/ImageReplaceHandler.properties");
            m_props.load(is);
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }
    
    /** 
     * This method forwards the request to the right candidate based on the
     * data source and image replacement handler defined for it.
     * <p>
     * If there is no image replacement handler defined then it sets the
     * status for the request to SC_NOT_IMPLEMENTED.
     *
     * @param p_request Request object
     * @param p_response Response object
     * @throws ServletException Exception encountered during this servlet execution.
     * @throws IOException Exception encountered during this servlet execution.
     */     
    public void service(HttpServletRequest p_request, 
                        HttpServletResponse p_response)
        throws ServletException, IOException
    {
        //  Determine who to forward this request to. Different data source
        //  has it's own way of dealing with files.
        //
        //  First, check if there is any image replace handler defined in
        //  "ImageReplaceHandler.properties" file. The format of this
        //  file is as follow
        //
        //  com.globalsight.ImageReplaceHandler.<sourcename>=<servletname>
        //
        //  If there is one defined, we forward the request to that servlet, else
        //  return a failure response back to requester.
        //

        String  eventFlowXml = p_request.getParameter(ExportConstants.EVENT_FLOW_XML);
        String  dataSource = getDataSource(eventFlowXml);
    
        String handlerName = null;
        try
        {
            handlerName = m_props.getProperty("com.globalsight.ImageReplaceHandler." + dataSource);
            Logger.getLogger().println(Logger.DEBUG_D,"Using handler " + handlerName);
        } catch (MissingResourceException mre)
        {
            // do nothing - let fall to the finally clause
        } finally
        {
            if (handlerName == null || handlerName == "")
            {
                p_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                theLogger.println(Logger.ERROR, "There is no image replacement handler defined for " + dataSource);

            //  Need to post a response back to the response URL
            //
            return;
            }
        }
        
        ServletContext sc = getServletConfig().getServletContext();
        RequestDispatcher rd = sc.getNamedDispatcher(handlerName);
        
        if (rd == null)
        {
            p_response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            theLogger.println(Logger.ERROR, "Unable to locate image replacement handler for " + dataSource);
            
            //  Need to post a response back to the response URL
            //
            return;
        }
        else 
        {
            rd.forward(p_request, p_response);
        }
    }

    /** 
     * Parse event flow XML and return the data source name
     * string.
     *
     * @param p_eventFlowXml Evemt flow XML of the original source.
     *
     * @return data source type name.
     *
     */    
    private String getDataSource(String p_eventFlowXml)
    throws ServletException
    {
        //  Parse the event flow Xml and determine what is the data source
        //
        try
        {
            String dataSource = null;
            StringReader sr = new StringReader(p_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
            parser.parse(is);
            Element rootElem = parser.getDocument().getDocumentElement();
            Element elem = (Element) rootElem.getElementsByTagName("source").item(0);
            dataSource = elem.getAttribute("dataSourceType");

            //see if the eventflowxml has been through the office adapter
            NodeList nl = rootElem.getElementsByTagName("category");
            boolean is_msoffice = false;
            for (int i =0; i < nl.getLength(); i++)
            {
                Element e  = (Element) nl.item(i);
                String name = e.getAttribute("name");
                if (name.equals("MicrosoftApplicationAdapter"))
                {
                    is_msoffice = true;
                    break;
                }
            }

            if (is_msoffice)
            {
                return "msoffice";
            }
            else
            {
                return dataSource;
            }
        }
        catch (Exception e)
        {
            theLogger.printStackTrace(Logger.ERROR,"Problem getting the data source type:",e);
            throw new ServletException(e.getMessage());
        }
    }
}
