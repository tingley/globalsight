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
package com.globalsight.cxe.persistence.knownformattype;

import java.util.Vector;

import com.globalsight.cxe.util.CxeProxy;

/**
 * KnownFormatTypeDescriptorModifier extends DescriptorModifier by providing
 * amendment methods unique to the KnownFormatType descriptor.
 */
public class KnownFormatTypeDescriptorModifier
{
    //
    // PRIVATE CONSTANTS
    //
    public static Long FORMAT_WORD = new Long(14);
    public static Long FORMAT_EXCEL = new Long(19);
    public static Long FORMAT_PPT = new Long(20);
    public static Long FORMAT_PDF = new Long(22);
    public static Long FORMAT_FRAME5 = new Long(15);
    public static Long FORMAT_QUARK = new Long(16);
    public static Long FORMAT_FRAME6 = new Long(17);
    public static Long FORMAT_FRAME7 = new Long(25);

    public static final String ID_ARG = "profileId";

    /**
     * Returns a list of format type IDs that should be excluded because the
     * corresponding adapters to use that format are not installed. For example,
     * if the MsOffice Adapter is not installed, then the IDs for doc,ppt,and
     * xls are excluded.
     * 
     * @return long[]
     */
    public static Vector getExcludedFormatIds()
    {
        Vector formats = new Vector();

//        if (CxeProxy.isMsOfficeAdapterInstalled() == false)
//        {
//            formats.add(FORMAT_WORD);
//            formats.add(FORMAT_EXCEL);
//            formats.add(FORMAT_PPT);
//        }
//
//        if (CxeProxy.isQuarkFrameAdapterInstalled() == false)
//        {
//            formats.add(FORMAT_FRAME5);
//            formats.add(FORMAT_QUARK);
//            formats.add(FORMAT_FRAME6);
//            formats.add(FORMAT_FRAME7);
//        }
//
//        if (CxeProxy.isPdfAdapterInstalled() == false)
//        {
//            formats.add(FORMAT_PDF);
//        }

        return formats;
    }
}
