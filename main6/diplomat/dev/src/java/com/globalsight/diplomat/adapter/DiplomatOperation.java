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
package com.globalsight.diplomat.adapter;
import com.globalsight.diplomat.util.Logger;

import java.io.StringReader;

//SAX,DOM
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//SQL
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

/**
 * DiplomatOperation
 * <p>
 * An abstract operation class that has support for pre and post processing.
 */
public abstract class DiplomatOperation
{
        //public methods
        /** Sets the processor class to be used for pre processing. The processor
        * class must implement the DiplomatProcessor interface.
        * <br>
        * @param p_preProcessorName The full java package name of the class to be used
        */
        public void setPreProcessorName (String p_preProcessorName)
        {
                m_preProcessorName = p_preProcessorName;
        }

        /** Gets the processor class to be used for pre processing. The processor
        * class must implement the DiplomatProcessor interface.
        * <br>
        * @return The full java package name of the class to be used
        */
        public String getPreProcessorName ()
        {
                return m_preProcessorName;
        }

        /** Sets the processor class to be used for post processing. The processor
        * class must implement the DiplomatProcessor interface.
        * <br>
        * @param p_preProcessorName The full java package name of the class to be used
        */
        public void setPostProcessorName (String p_postProcessorName)
        {
                m_postProcessorName = p_postProcessorName;
        }

        /** Gets the processor class to be used for post processing. The processor
        * class must implement the DiplomatProcessor interface.
        * <br>
        * @param p_preProcessorName The full java package name of the class to be used
        */
        public String getPostProcessorName ()
        {
                return m_postProcessorName;
        }

        /** Loads the configured preprocessor if possible.
        * @return Returns the configured DiplomatProcessor for preprocessing (may be null)
        */
        public DiplomatProcessor loadPreProcessor ()
        {
            DiplomatProcessor theProcessor = null;
            Logger logger = Logger.getLogger();
            String p_processorName = getPreProcessorName();
            if (!(null == p_processorName || p_processorName.equals("")))
                {
                try {
                    Class processorClass = Class.forName(p_processorName);
                    theProcessor = (DiplomatProcessor) processorClass.newInstance();
                }
                catch (ClassNotFoundException cnfe)
                {
                    logger.printStackTrace(Logger.ERROR,
                                           "Pre-processor class " + p_processorName + " is not found.", cnfe);
                    theProcessor = null;
                }
                catch (Exception e)
                {
                    logger.printStackTrace(Logger.ERROR,
                                           "Pre-processor class " + p_processorName + " construction exception.", e);
                    theProcessor = null;
                }
            }
            return theProcessor;
        }
        
        /** Loads the configured post-processor if possible.
        * @return Returns the configured DiplomatProcessor for post-processing (may be null)
        */
        public DiplomatProcessor loadPostProcessor ()
        {
            DiplomatProcessor theProcessor = null;
            Logger logger = Logger.getLogger();
            String p_processorName = getPostProcessorName();
            if (!(null == p_processorName || p_processorName.equals("")))
                {
                try {
                    Class processorClass = Class.forName(p_processorName);
                    theProcessor = (DiplomatProcessor) processorClass.newInstance();
                }
                catch (ClassNotFoundException cnfe)
                {
                    logger.printStackTrace(Logger.ERROR,
                           "Post-processor class " + p_processorName + " is not found.", cnfe);
                    theProcessor = null;
                }
                catch (Exception e)
                {
                    logger.printStackTrace(Logger.ERROR,
                           "Post-processor class " + p_processorName + " construction exception.", e);
                    theProcessor = null;
                }
            }
            return theProcessor;
        }


           /** Reads a SystemParameter from the DB
           **@return null if the paramter does not exist*/
           public static String readSystemParameter(String p_parameter)
           {
               String value = null;
               Connection connection = null;
               Logger theLogger = Logger.getLogger();
               PreparedStatement query = null;

               try {
                   connection =  ConnectionPool.getConnection();
                   String sql = "SELECT VALUE FROM SYSTEM_PARAMETER WHERE NAME=?";
                   query = connection.prepareStatement(sql);
                   query.setString(1,p_parameter);
                   ResultSet results = query.executeQuery();
                   if (results.next())
                       value = results.getString(1);
                   else
                       {
                       theLogger.println(Logger.ERROR, "System Parameter " + p_parameter +
                                          " does not exist.");
                       value = null;
                   }
               }
               catch (Exception e)
               {
                   theLogger.printStackTrace(Logger.ERROR,"Problem reading system parameter " +
                                             p_parameter + " from the DB", e);
                   value = null;
               }
               finally
               {
                   if (query!=null)
                   {
                       try {
                           query.close();
                       }
                       catch (Exception e) {}
                   }
                   try { ConnectionPool.returnConnection(connection); }
                   catch (ConnectionPoolException cpe) {}
               }

               return value;
           }

        /* Only called prior to throwing an exception. It parses the EventFlowXml
        * and returns the content display name*/
     protected String getDisplayName(String p_eventFlowXml)
     {
         String name = null;
         try {
             StringReader sr = new StringReader(p_eventFlowXml);
             InputSource is = new InputSource(sr);
             DOMParser parser = new DOMParser();
             parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
             parser.parse(is);
             Element elem = parser.getDocument().getDocumentElement();
             NodeList nl = elem.getElementsByTagName("displayName");
             Element displayNameElement = (Element) nl.item(0);
             name = displayNameElement.getFirstChild().getNodeValue();
         }
         catch (Exception e)
         {  
             //since this is already an error condition, just log it
             Logger.getLogger().printStackTrace(Logger.ERROR,"Could not parse EventFlowXml: ", e);
         }
         return name;
     }

     /**
      * Reads the content from the named temp file
      * and then deletes the file.
      * 
      * @param fileName the tmp filename containing the event data
      * @return byte[]
      */
     public static byte[] readContentFromTmpFile(String fileName)
     throws Exception
     {
	 File f = new File (fileName);
	 FileInputStream fis = new FileInputStream(fileName);
	 byte b[] = new byte[(int)f.length()];
	 fis.read(b);
	 fis.close();
	 f.delete();
	 return b;
     }

     /**
      * Reads the String from the named temp file
      * and then deletes the file.
      * 
      * @param fileName the tmp filename containing the event data
      * @return String
      */
     public static String readStringFromTmpFile(String fileName)
     throws Exception
     {
	 byte b[] = readContentFromTmpFile(fileName);
	 return new String(b,"UTF8");
     }



     
     //constructor
     protected DiplomatOperation()
     {
     }

        //private data
        private String m_preProcessorName =  null;
        private String m_postProcessorName = null;
}
