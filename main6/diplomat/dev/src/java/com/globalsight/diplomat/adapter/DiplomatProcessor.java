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

/**
 * DiplomatProcessor
 * <p>
 * An interface to allow pre- and post-processing.
 */
public interface DiplomatProcessor
{

    /** Sets the processor's internal reference to the Event Flow Xml 
     * @param the event flow xml */
    public void setEventFlowXml(String p_eventFlowXml);

    /** Gets the event flow xml */
    public String getEventFlowXml();

    /** Sets the processor's internal reference to the content as a unicode string
     * @param the content*/
    public void setUnicodeContent(String p_content);

    /** Gets the unicode content*/
    public String getUnicodeContent();

    /** Sets the processor's internal reference to binary content as a byte array
     * @param the content*/
    public void setBinaryContent(byte[] p_content);

    /** Gets the unicode content*/
    public byte[] getBinaryContent();

    /** Performs arbitrary processing given the EventFlowXml
    * and the content (in an original format like HTML, or in
    * DiplomatXml, etc.) or in binary.
    */
    public void process ();
}

