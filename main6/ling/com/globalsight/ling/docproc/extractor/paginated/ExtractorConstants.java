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
package com.globalsight.ling.docproc.extractor.paginated;


public interface ExtractorConstants
{
    public static final String NODE_COLUMN = "column";
    public static final String NODE_LABEL = "label";
    public static final String NODE_CONTENT = "content";
    public static final String NODE_COLUMNHEADER = "columnHeader";
    public static final String NODE_ROW = "row";

    public static final String ATT_CONTENT_MODE = "contentMode";
    public static final String ATT_DATA_TYPE = "dataType";
    public static final String ATT_RULE_ID = "ruleId";
    public static final String ATT_VALUE_INVISIBLE = "invisible";

    public static final String DTD_NAME = "PaginatedResultSetXml.dtd";

    public static final String DEFAULT_DATA_FORMAT = "text";
    public static final String LABEL_DATA_FORMAT = "text";

    public static final String EXTRACT_LABEL = "extract-label";
    public static final String EXTRACT_CONTENT = "extract-content";
    
}
