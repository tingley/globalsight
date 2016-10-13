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
package com.globalsight.cxe.adapter.quarkframe;

//JDK
import java.io.StringReader;

import org.apache.log4j.Logger;

//DOM
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;

/** 
* The DesktopAppHelperFactory can be used to create the appropriate DesktopAppHelper
* based on the file that needs to be converted to XML.
*/
public class DesktopAppHelperFactory
{
    /* Types of helpers the factory can make*/
    private static final int QUARK = 2;
    private static final int FRAME = 3;

    /** Creates a DesktopAppHelper based on the FileProfile specified in the
    * EventFlowXml
    * <br>
    * @param p_workingDir -- the main working directory where the conversion server looks for files
    * @param p_eventFlowXml -- the EventFlowXml
    * @param p_content -- the content (whether GXML or Native)
    */
    public static DesktopAppHelper createDesktopAppHelper(String p_workingDir,
                                                          CxeMessage p_cxeMessage,
                                                          Logger p_logger,
                                                          String p_formatType)
    throws DesktopApplicationAdapterException
    {
        switch (determineFormatType(p_formatType))
        {
        case QUARK:
            return new QuarkHelper(p_workingDir,p_cxeMessage,p_logger);
        case FRAME:
            return new FrameHelper(p_workingDir,p_cxeMessage,p_logger);
        default:
            throw new DesktopApplicationAdapterException("Unexpected", null, null);
        }
    }

    /** Determines the format type
    * <br>
    * @param p_formatType -- a string like "word","quark",etc.
    * @return format type -- WORD,QUARK,FRAME
    * @throws DesktopApplicationAdapterException
    */
    private static int determineFormatType(String p_formatType)
    throws DesktopApplicationAdapterException
    {
        if (p_formatType.equals("quark"))
            return QUARK;
        else if (p_formatType.equals("frame"))
            return FRAME;
        else
            {
            //throw an error for an unhandled format
            String[] errorArgs = {p_formatType};
            throw new DesktopApplicationAdapterException("UnHandled",errorArgs,null);
        }
    }
}

