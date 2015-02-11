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
package com.globalsight.cxe.entity.knownformattype;

/**
 * Represents a CXE Known Format Type entity object.
 */
public interface KnownFormatType
{
    // These formats are not a complete list, but do need to be the
    // exact same as values in the DB because the code may refer to
    // them with the following names.
    public final String WORD = "Word";
    public final String POWERPOINT = "PowerPoint";
    public final String PDF = "PDF";
    public final String EXCEL = "Excel";
    public final String RTF = "RTF";
    public final String QUARK = "Quark";
    public final String QUARKMAC = "Quark (MAC)";
    public final String FRAME5 = "Frame5";
    public final String FRAME6 = "Frame6";
    public final String FRAME7 = "Frame7";
    public final String FRAME = "Frame";
    public final String INDD = "Indd";
    public final String XML = "XML";

    /**
    ** Return the id of the KnownFormatType (cannot be set)
    ** @return id as a long
    **/
    public long getId();

    /**
     * Return the name of the known format type
     * @return known format type name
     */
    public String getName();

    /**
     * Return the description of the known format type
     * @return known format type description
     */
    public String getDescription();

    /**
     * Return the format type of the known format type
     * @return known format type format type
     */
    public String getFormatType();

    /**
     * Return the connection string of the known format type
     * @return known format type string
     */
    public String getPreExtractEvent();

    /**
     * Return the user name of the known format type
     * @return known format type user name
     */
    public String getPreMergeEvent();

    /**
     * Set the name of the known format type
     */
    public void setName(String p_name);

    /**
     * Set the description of the known format type
     * @param p_description The description of the known format type
     */
    public void setDescription(String p_description);

    /**
     * Set the driver of the known format type
     * @param p_format_type The driver of the known format type
     */
    public void setFormatType(String p_format_type);

    /**
     * Set the pre_extract_event of the known format type
     * @param p_pre_extract_event The pre_extract_event of the known format type
     */
    public void setPreExtractEvent(String p_pre_extract_event);

    /**
     * Set the pre_merge_event of the known format type
     * @param p_pre_merge_event The pre_merge_event of the known format type
     */
    public void setPreMergeEvent(String p_pre_merge_event);

}

