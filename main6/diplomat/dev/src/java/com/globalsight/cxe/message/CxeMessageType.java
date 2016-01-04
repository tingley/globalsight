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
package com.globalsight.cxe.message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * The CxeMessageType class contains all the definitions of CXE
 * Message types (event types)
 */
public class CxeMessageType implements Serializable
{
	private static final long serialVersionUID = -6106621398313197845L;

	//////////////////////////////////////
    // Public Constants                 //
    //////////////////////////////////////

    // NOTE: When you add new constants to this section, but sure to
    // update the static{} section below where fillMaps() is called
    // for each constant to make sure that the hashmap is properly
    // filled.
    //
    // Format related constants are in the range 0-500.
    //
    // You can't do something fancy here like use i++ for each value
    // because Java switch statements won't handle that then... gotta
    // wait for enums in Java.
    public static final int GXML_CREATED_EVENT = 0;
    public static final int GXML_LOCALIZED_EVENT = 1;

    public static final int HTML_IMPORTED_EVENT = 2;
    public static final int HTML_LOCALIZED_EVENT = 3;

    public static final int XML_IMPORTED_EVENT = 4;
    public static final int XML_LOCALIZED_EVENT = 5;

    public static final int MSOFFICE_IMPORTED_EVENT = 6;
    public static final int FRAME_IMPORTED_EVENT = 7;
    public static final int QUARK_IMPORTED_EVENT = 8;
    public static final int PDF_IMPORTED_EVENT = 9;

    public static final int UNEXTRACTED_IMPORTED_EVENT = 10;
    public static final int UNEXTRACTED_LOCALIZED_EVENT = 11;

    public static final int MSOFFICE_LOCALIZED_EVENT = 12;
    public static final int PDF_LOCALIZED_EVENT = 13;
    public static final int FRAME_LOCALIZED_EVENT = 14;
    public static final int QUARK_LOCALIZED_EVENT = 15;

    public static final int PRSXML_IMPORTED_EVENT = 16;
    public static final int PRSXML_LOCALIZED_EVENT = 17;

    public static final int RTF_IMPORTED_EVENT = 18;
    public static final int RTF_LOCALIZED_EVENT = 19;

    public static final int COPYFLOW_IMPORTED_EVENT = 20;
    public static final int COPYFLOW_LOCALIZED_EVENT = 21;

    public static final int XPTAG_IMPORTED_EVENT = 22;
    public static final int XPTAG_LOCALIZED_EVENT = 23;

    // add adobe related constants
    public static final int ADOBE_IMPORTED_EVENT = 24;
    public static final int ADOBE_LOCALIZED_EVENT = 25;
    
    // add open office related constants
    public static final int OPENOFFICE_IMPORTED_EVENT = 26;
    public static final int OPENOFFICE_LOCALIZED_EVENT = 27;
    
    // add idml related constants
    public static final int IDML_IMPORTED_EVENT = 28;
    public static final int IDML_LOCALIZED_EVENT = 29;
    
    public static final int MIF_LOCALIZED_EVENT = 30;
    public static final int MIF_IMPORTED_EVENT = 31;
    
    public static final int PASSOLO_LOCALIZED_EVENT = 32;
    public static final int PASSOLO_IMPORTED_EVENT = 33;
    
    public static final int WINPE_LOCALIZED_EVENT = 34;
    public static final int WINPE_IMPORTED_EVENT = 35;
    
    // data source related constants
    public static final int FILE_SYSTEM_FILE_SELECTED_EVENT = 500;
    public static final int FILE_SYSTEM_EXPORT_EVENT = 501;
    public static final int CXE_EXPORT_STATUS_EVENT = 502;
    public static final int CAP_EXPORT_STATUS_EVENT = 503;
    public static final int CXE_IMPORT_ERROR_EVENT = 504;
    public static final int CAP_IMPORT_ERROR_EVENT = 505;
    public static final int STF_CREATION_EVENT = 506;

    public static final int VIGNETTE_FILE_SELECTED_EVENT = 509;
    public static final int VIGNETTE_EXPORT_EVENT = 510;

    public static final int DATABASE_EXPORT_EVENT = 511;
    public static final int DYNAMIC_PREVIEW_EVENT = 512;

    public static final int MEDIASURFACE_FILE_SELECTED_EVENT = 513;
    public static final int MEDIASURFACE_EXPORT_EVENT = 514;

    public static final int CATALYST_IMPORTED_EVENT = 515;
    public static final int CATALYST_LOCALIZED_EVENT = 516;

    public static final int SERVICEWARE_FILE_SELECTED_EVENT = 517;
    public static final int SERVICEWARE_EXPORT_EVENT = 518;

    public static final int DOCUMENTUM_FILE_SELECTED_EVENT = 521;
    public static final int DOCUMENTUM_EXPORT_EVENT = 522;

    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private static HashMap s_byNames;
    private static HashMap s_byValues;
    private String m_name;
    private int m_value;

    /**
     * Fills two hashmaps for keeping track of CxeMessageType
     * objects. One indexed by the event name, the other
     * indexed by the event integer value.
     *
     * @param p_eventValue event value as int
     * @param p_eventName event name as String
     */
    private static void fillMaps(int p_eventValue, String p_eventName)
    {
        Integer i = new Integer(p_eventValue);
        CxeMessageType cxeMessageType =
            new CxeMessageType(p_eventValue, p_eventName);

        s_byNames.put(p_eventName, cxeMessageType);
        s_byValues.put(i, cxeMessageType);
    }

    // Fill all the hashmaps with names and values. If you add a new
    // event type above be sure to add the entry to this section!!
    static
    {
        s_byNames = new HashMap(50);
        s_byValues = new HashMap(50);
        fillMaps(GXML_CREATED_EVENT,"GXML_CREATED_EVENT");
        fillMaps(GXML_LOCALIZED_EVENT,"GXML_LOCALIZED_EVENT");
        fillMaps(HTML_IMPORTED_EVENT,"HTML_IMPORTED_EVENT");
        fillMaps(HTML_LOCALIZED_EVENT,"HTML_LOCALIZED_EVENT");
        fillMaps(XML_IMPORTED_EVENT,"XML_IMPORTED_EVENT");
        fillMaps(XML_LOCALIZED_EVENT,"XML_LOCALIZED_EVENT");
        fillMaps(XPTAG_IMPORTED_EVENT,"XPTAG_IMPORTED_EVENT");
        fillMaps(XPTAG_LOCALIZED_EVENT,"XPTAG_LOCALIZED_EVENT");
        fillMaps(FILE_SYSTEM_FILE_SELECTED_EVENT,"FILE_SYSTEM_FILE_SELECTED_EVENT");
        fillMaps(FILE_SYSTEM_EXPORT_EVENT,"FILE_SYSTEM_EXPORT_EVENT");
        fillMaps(CXE_EXPORT_STATUS_EVENT,"CXE_EXPORT_STATUS_EVENT");
        fillMaps(CAP_EXPORT_STATUS_EVENT,"CAP_EXPORT_STATUS_EVENT");
        fillMaps(CXE_IMPORT_ERROR_EVENT, "CXE_IMPORT_ERROR_EVENT");
        fillMaps(CAP_IMPORT_ERROR_EVENT, "CAP_IMPORT_ERROR_EVENT");

        //msoffice/pdf
        fillMaps(MSOFFICE_IMPORTED_EVENT,"MSOFFICE_IMPORTED_EVENT");
        fillMaps(MSOFFICE_LOCALIZED_EVENT,"MSOFFICE_LOCALIZED_EVENT");
        fillMaps(PDF_IMPORTED_EVENT,"PDF_IMPORTED_EVENT");
        fillMaps(PDF_LOCALIZED_EVENT,"PDF_LOCALIZED_EVENT");

        //quark/frame
        fillMaps(FRAME_IMPORTED_EVENT,"FRAME_IMPORTED_EVENT");
        fillMaps(QUARK_IMPORTED_EVENT,"QUARK_IMPORTED_EVENT");
        fillMaps(QUARK_LOCALIZED_EVENT,"QUARK_LOCALIZED_EVENT");
        fillMaps(FRAME_LOCALIZED_EVENT,"FRAME_LOCALIZED_EVENT");

        //adobe
        fillMaps(ADOBE_IMPORTED_EVENT, "ADOBE_IMPORTED_EVENT");
        fillMaps(ADOBE_LOCALIZED_EVENT, "ADOBE_LOCALIZED_EVENT");
        
        //open office
        fillMaps(OPENOFFICE_IMPORTED_EVENT, "OPENOFFICE_IMPORTED_EVENT");
        fillMaps(OPENOFFICE_LOCALIZED_EVENT, "OPENOFFICE_LOCALIZED_EVENT");
        
        //Idml
        fillMaps(IDML_IMPORTED_EVENT, "IDML_IMPORTED_EVENT");
        fillMaps(IDML_LOCALIZED_EVENT, "IDML_LOCALIZED_EVENT");
        
        //GlobalSightXT (CopyFlow) (Quark)
        fillMaps(COPYFLOW_IMPORTED_EVENT,"COPYFLOW_IMPORTED_EVENT");
        fillMaps(COPYFLOW_LOCALIZED_EVENT,"COPYFLOW_LOCALIZED_EVENT");

        //unextracted/stf
        fillMaps(UNEXTRACTED_IMPORTED_EVENT,"UNEXTRACTED_IMPORTED_EVENT");
        fillMaps(UNEXTRACTED_LOCALIZED_EVENT,"UNEXTRACTED_LOCALIZED_EVENT");
        fillMaps(STF_CREATION_EVENT,"STF_CREATION_EVENT");

        //rtf
        fillMaps(RTF_IMPORTED_EVENT,"RTF_IMPORTED_EVENT");
        fillMaps(RTF_LOCALIZED_EVENT,"RTF_LOCALIZED_EVENT");

        //vignette
        fillMaps(VIGNETTE_FILE_SELECTED_EVENT,"VIGNETTE_FILE_SELECTED_EVENT");
        fillMaps(VIGNETTE_EXPORT_EVENT,"VIGNETTE_EXPORT_EVENT");

        //database
        fillMaps(DATABASE_EXPORT_EVENT, "DATABASE_EXPORT_EVENT");
        fillMaps(PRSXML_LOCALIZED_EVENT, "PRSXML_LOCALIZED_EVENT");
        fillMaps(PRSXML_IMPORTED_EVENT, "PRSXML_IMPORTED_EVENT");
        fillMaps(DYNAMIC_PREVIEW_EVENT, "DYNAMIC_PREVIEW_EVENT");

        //mediasurface
        fillMaps(MEDIASURFACE_FILE_SELECTED_EVENT, "MEDIASURFACE_FILE_SELECTED_EVENT");
        fillMaps(MEDIASURFACE_EXPORT_EVENT, "MEDIASURFACE_EXPORT_EVENT");

        //catalyst
        fillMaps(CATALYST_IMPORTED_EVENT, "CATALYST_IMPORTED_EVENT");
        fillMaps(CATALYST_LOCALIZED_EVENT, "CATALYST_LOCALIZED_EVENT");

       //serviceware
        fillMaps(SERVICEWARE_FILE_SELECTED_EVENT, "SERVICEWARE_FILE_SELECTED_EVENT");
        fillMaps(SERVICEWARE_EXPORT_EVENT, "SERVICEWARE_EXPORT_EVENT");

       //documentum
        fillMaps(DOCUMENTUM_FILE_SELECTED_EVENT, "DOCUMENTUM_FILE_SELECTED_EVENT");
        fillMaps(DOCUMENTUM_EXPORT_EVENT, "DOCUMENTUM_EXPORT_EVENT");
        
        //mif
        fillMaps(MIF_IMPORTED_EVENT, "MIF_IMPORTED_EVENT");
        fillMaps(MIF_LOCALIZED_EVENT, "MIF_LOCALIZED_EVENT");
        
        //Passolo
        fillMaps(PASSOLO_IMPORTED_EVENT, "PASSOLO_IMPORTED_EVENT");
        fillMaps(PASSOLO_LOCALIZED_EVENT, "PASSOLO_LOCALIZED_EVENT");
        
        //windows pe
        fillMaps(WINPE_IMPORTED_EVENT, "WINPE_IMPORTED_EVENT");
        fillMaps(WINPE_LOCALIZED_EVENT, "WINPE_LOCALIZED_EVENT");
    }

    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    /**
     * Creates a CxeMessageType object
     *
     * @param p_value int value
     * @param p_name  String name
     */
    private CxeMessageType(int p_value, String p_name)
    {
        m_value = p_value;
        m_name = p_name;
    }

    //////////////////////////////////////
    // Public Methods                   //
    //////////////////////////////////////
    /**
     * Looks up a CxeMessageType object based on the
     * int value of the event.
     *
     * @param p_eventValue one of the defined constants in
     * CxeMessageType
     * @return CxeMessageType
     * @exception NoSuchElementException
     */
    public static CxeMessageType getCxeMessageType(int p_eventValue)
    throws NoSuchElementException
    {
        CxeMessageType cmt = (CxeMessageType)s_byValues.get(
            new Integer(p_eventValue));

        if (cmt == null)
        {
            throw new NoSuchElementException(
                "No CxeMessageType for event value " + p_eventValue);
        }

        return cmt;
    }

    /**
     * Looks up a CxeMessageType object based on the name of the event.
     *
     * @param p_eventName the english name of an event
     * @return CxeMessageType
     * @exception java.util.NoSuchElementException
     */
    public static CxeMessageType getCxeMessageType(String p_eventName)
        throws java.util.NoSuchElementException
    {
        CxeMessageType cmt = (CxeMessageType) s_byNames.get(p_eventName);

        if (cmt == null)
        {
            cmt = getNewEventNameMessageType(p_eventName);
        }

        if (cmt == null)
        {
            throw new NoSuchElementException(
                "No CxeMessageType for event name " + p_eventName);
        }

        return cmt;
    }


    /**
     * For handling data migration. If the event name is a pre 4.5
     * name then try to figure out what the new event name is.
     *
     * @param p_oldEventName old Event Name
     * @return CxeMessageType or null
     */
    private static CxeMessageType getNewEventNameMessageType(
        String p_oldEventName)
    {
        int newEventValue = -1;

        if (p_oldEventName.indexOf("ImportedEvent") > 0)
        {
            if (p_oldEventName.indexOf("Word") > 0)
                newEventValue= MSOFFICE_IMPORTED_EVENT;
            else if (p_oldEventName.indexOf("Frame") > 0)
                newEventValue = FRAME_IMPORTED_EVENT;
            else if (p_oldEventName.indexOf("Quark") > 0)
                newEventValue = QUARK_IMPORTED_EVENT;
            else if (p_oldEventName.indexOf("Pdf") > 0)
                newEventValue = QUARK_IMPORTED_EVENT;
            else
                newEventValue = HTML_IMPORTED_EVENT;
        }
        else if (p_oldEventName.indexOf("TranslatedEvent") > 0)
        {
            if (p_oldEventName.indexOf("Xml") > 0)
                newEventValue= XML_LOCALIZED_EVENT;
            else if (p_oldEventName.indexOf("Word") > 0)
                newEventValue = MSOFFICE_LOCALIZED_EVENT;
            else if (p_oldEventName.indexOf("Frame") > 0)
                newEventValue = FRAME_LOCALIZED_EVENT;
            else if (p_oldEventName.indexOf("Quark") > 0)
                newEventValue = QUARK_LOCALIZED_EVENT;
            else if (p_oldEventName.indexOf("Pdf") > 0)
                newEventValue = PDF_LOCALIZED_EVENT;
            else
                newEventValue = HTML_LOCALIZED_EVENT;
        }
        else if (p_oldEventName.indexOf("MergedEvent") > 0)
        {
            if (p_oldEventName.indexOf("Database") > 0)
                newEventValue= DATABASE_EXPORT_EVENT;
            else if (p_oldEventName.indexOf("Vignette") > 0)
                newEventValue = VIGNETTE_EXPORT_EVENT;
            else
                newEventValue = FILE_SYSTEM_EXPORT_EVENT;
        }

        if (newEventValue == -1)
        {
            return null;
        }
        else
        {
            return (CxeMessageType)s_byValues.get(
                new Integer(newEventValue));
        }
    }

    /**
     * Returns an english event name
     *
     * @return String
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the integer value of the event
     *
     * @return int
     */
    public int getValue()
    {
        return m_value;
    }

    /**
     * Returns a string representation of the event.
     * That is the name of the event.
     *
     * @return String
     */
    public String toString()
    {
        return m_name;
    }
}