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
import org.apache.log4j.Logger;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.CxeMessage;

/**
* The QuarkHelper handles converting Quark to and from XML.
*/
public class QuarkHelper extends DesktopAppHelper
{
    private static final String FORMAT = "quark";

    private static final String PROPERTY_FILE =
        "/properties/QuarkWinAdapter.properties";

    /**
     * Constructs a QuarkHelper
     *
     * @param p_workingDir -- the main working directory where the
     * conversion server looks for files
     * @param p_eventFlowXml -- the EventFlowXml
     * @param p_content -- the content (whether GXML or Native)
     */
    public QuarkHelper(String p_workingDir, CxeMessage p_cxeMessage,
        Logger p_logger)
    {
        super(p_workingDir, p_cxeMessage, p_logger);
    }

    /**************************************************/
    /*** Methods over-riding the super class methods ***/
    /**************************************************/

    /**
     * Returns the format name that this desktop app helper supports.
     *
     * @return "quark"
     */
    public String getFormatName()
    {
        return FORMAT;
    }

    /**
     * Returns the event to use as the post merge event
     * so that after the merger merges the GXML to XML,
     * the XML will come to the DesktopApplicationAdapter
     * <br>
     * @return post merge event name
     */
    protected CxeMessageType getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(
            CxeMessageType.QUARK_LOCALIZED_EVENT);
    }

    /**
     * Returns the name of the property file for this helper.
     */
    protected String getPropertyFileName()
    {
        return PROPERTY_FILE;
    }
}
