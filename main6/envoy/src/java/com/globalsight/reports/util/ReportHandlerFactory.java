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
package com.globalsight.reports.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.globalsight.reports.Constants;
import com.globalsight.reports.handler.BasicReportHandler;


public class ReportHandlerFactory 
{
   private static final Logger CATEGORY =
         Logger.getLogger( ReportHandlerFactory.class.getName() );
    
    private static HashMap<String, String> reportHandlerMap = new HashMap<String, String>();
    private static HashMap<String, String> reportTargetUrlMap = new HashMap<String, String>();
    private static HashMap<String, BasicReportHandler> m_pageHandlers = new HashMap<String, BasicReportHandler>();
    
    //  Singleton instance.
    // (Don't call instance, call the static method below.)
    private static ReportHandlerFactory instance = null;
       
    /**
     * Default constructor.
     */
    private ReportHandlerFactory()
    {
        // Default constructor.
    }
    
    /**
     * Returns a the singleton instance of this ReportHandlerFactory
     *
     * @return The singleton ReportHandlerFactory.
     */
    public static ReportHandlerFactory instance()
    {
        if( instance == null )
        {
            instance = new ReportHandlerFactory();
        }
        
        return instance;
    }
    
    /**
     * Indicates whether the site description is initialized or not
     *
     * @return <code>true</code> if is initialized, <code>false</code>
     * if not
     */
    public static boolean isInitialized()
    {
        return ( instance != null );
    }
    
    /**
     * Reads in the Report handler mapping from an XML file, parses it
     * and populates this data structure
     *
     * @param reportConfigXML the name of the XML file that
     * holds the report mapping.
     * @return <code>true</code> if successful, <code>false</code>
     * otherwise.
     */
    public static boolean createReportHandlerMap( String reportConfigXML )
    {
        boolean retVal = false;
        SAXReader reader = new SAXReader(); 
        Document document = null; 
           
        try 
        {
            InputStream is = ReportHandlerFactory.class
                    .getResourceAsStream(reportConfigXML);
            document = reader.read( new InputSource( is ) );
            
            // parse XML file in order to get className counterpart to pagename
            List<?> moduleList = document.selectNodes( 
                    Constants.REPORTMODULE_NODE_XPATH );
            for( Iterator<?> iter = moduleList.iterator(); iter.hasNext(); )
            {
                Element element = ( Element )iter.next();
                Attribute attribute = 
                    element.attribute( Constants.REPORTNAME_ATTRIBUTE );
                Element elementNode = 
                    element.element( Constants.REPORTHANDLER_NODE );
                
                Attribute attributeNode = 
                    elementNode.attribute( Constants.CLASS_ATTRIBUTE );

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug( "CreateReportHandlerMap key: " + 
                            attribute.getValue() + 
                            " value: " + attributeNode.getValue() );                    
                }

                reportHandlerMap.put( attribute.getValue(), 
                                      attributeNode.getValue() );
            }
            
            // parse XML file in order to get target Url counterpart to pagename 
            List<?> urlList = 
                document.selectNodes( Constants.REPORTURL_NODE_XPATH );
            for(Iterator<?> iterUrl = urlList.iterator(); iterUrl.hasNext(); )
            {
                Element urlElement = (Element) iterUrl.next();
                Attribute jspNameAttribute = 
                    urlElement.attribute( Constants.JSPNAME_ATTRIBUTE );
                Attribute urlAttribute = 
                    urlElement.attribute( Constants.TARGETURL_ATTRIBUTE );
                reportTargetUrlMap.put( jspNameAttribute.getValue(),
                                        urlAttribute.getValue() );
            }

            if ( instance == null )
            {
                instance = new ReportHandlerFactory();
                retVal = true;
            }
            
        } 
        catch (DocumentException e) 
        { 
        	CATEGORY.error( "Cannot read the ReportConfig.xml", e );
        } 
        
        return retVal;
    }

    /**
     * Creates an instance of a report handler given its class name.
     *
     * @param thePageHandlerClassName the fully qualified class name
     * of a page handler
     *
     * @return an instance of a page handler if one is found, null
     * otherwise
     */
    public static BasicReportHandler getReportHandlerInstance(
            String theReportHandlerName )
    {       
        String theReportHandlerClassName = 
            (String)reportHandlerMap.get( theReportHandlerName );
        BasicReportHandler theReportHandler = 
            (BasicReportHandler)m_pageHandlers.get(theReportHandlerClassName);
        if ( theReportHandler == null )
        {
            try
            {
            	CATEGORY.debug( "Will get the " + 
                                theReportHandlerName + " instance" );
                theReportHandler = (BasicReportHandler)
                    Class.forName(theReportHandlerClassName).newInstance();
                theReportHandler.init();
                m_pageHandlers.put( theReportHandlerClassName,
                                    theReportHandler );
            }
            catch ( Exception e ) 
            {
                CATEGORY.error( "Cannot create report handler instance: " + 
                                theReportHandlerName );
                CATEGORY.error( e.getMessage(), e );
            }
        } 
        
        return theReportHandler;
    }
    
    public static String getTargetUrl( String jspName )
    {
        return reportTargetUrlMap.get( jspName ).toString();
    }

}
