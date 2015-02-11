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
package com.globalsight.util.gxml;

import com.globalsight.ling.common.DiplomatNames;

/**
 * GxmlNames contains the name constants used by Gxml,
 * such as Element tag name and
 * attribute names.
 */
public final class GxmlNames
{
    // TEXT_NODE name
    public static final String TEXT = "text";
    //PRS XML datatype - NOT in the DTD
    public static final String PRS_DATATYPE = "xml";


    //element in both GXML and PRS
    public static final String  GXML_ROOT = "diplomat";
    public static final String  TRANSLATABLE = "translatable";
    public static final String  SEGMENT = "segment";
    public static final String  LOCALIZABLE = "localizable";
    public static final String  SKELETON = "skeleton";
    public static final String  GS = DiplomatNames.Element.GSA;

    public static final String BPT = "bpt";
    public static final String EPT = "ept";
    public static final String SUB = "sub";
    public static final String IT  = "it";
    public static final String PH  = "ph";
    public static final String UT  = "ut";

    //Element in Paginated Result Set only
    public static final String PRS_ROOT="paginatedResultSetXml";
    public static final String RECORD="record";
    public static final String ACQSQLPARM="acqSqlParm";
    public static final String COLUMN="column";
    public static final String COLUMN_HEADER="columnHeader";
    public static final String CONTENT="content";
    public static final String LABEL="label";
    public static final String CONTEXT="context";
    public static final String ROW="row";

    //attributes for GXML root tag
    public static final String  GXMLROOT_VERSION = "version";
    public static final String  GXMLROOT_LOCALE = "locale";
    public static final String  GXMLROOT_DATATYPE = "datatype";
    public static final String  GXMLROOT_WORDCOUNT = "wordcount";
    public static final String  GXMLROOT_TARGETENCODING = "targetEncoding";

    //attributes for <translatable> tag
    public static final String  TRANSLATABLE_BLOCKID = "blockId";
    public static final String  TRANSLATABLE_TYPE = "type";
    public static final String  TRANSLATABLE_WORDCOUNT = "wordcount";
    public static final String  TRANSLATABLE_DATATYPE = "datatype";

    //attributes for <localizable> tag
    public static final String  LOCALIZABLE_BLOCKID = "blockId";
    public static final String  LOCALIZABLE_TYPE = "type";
    public static final String  LOCALIZABLE_WORDCOUNT = "wordcount";
    public static final String  LOCALIZABLE_DATATYPE = "datatype";

    //attributes for <segment> tag
    public static final String  SEGMENT_SEGMENTID = "segmentId";
    public static final String  SEGMENT_WORDCOUNT = "wordcount";
    public static final String  SEGMENT_PRESERVEWS = "preserveWhiteSpace";

    //attributes for <bpt> tag
    public static final String  BPT_I = "i";
    public static final String  BPT_TYPE = "type";
    public static final String  BPT_X = "x";
    public static final String  BPT_ERASEABLE = "erasable";
    public static final String  BPT_MOVEABLE = "movable";
    public static final String  BPT_PRESERVEWS = "preserveWhiteSpace";

    //attributes for <ept> tag
    public static final String  EPT_I = "i";
    public static final String  EPT_TYPE = "type";

    //attributes for <sub> tag
    public static final String  SUB_LOCTYPE = "locType";
    public static final String  SUB_DATATYPE = "datatype";
    public static final String  SUB_TYPE = "type";
    public static final String  SUB_ID = "id";
    public static final String  SUB_WORDCOUNT = "wordcount";
    public static final String  SUB_PRESERVEWS = "preserveWhiteSpace";

    //attributes for <it> tag
    public static final String  IT_POS = "pos";
    public static final String  IT_TYPE = "type";
    public static final String  IT_X = "x";
    public static final String  IT_I = "i";
    public static final String  IT_ERASEABLE = "erasable";
    public static final String  IT_MOVEABLE = "movable";

    //attributes for <ph> tag
    public static final String  PH_ASSOC = "assoc";
    public static final String  PH_TYPE = "type";
    public static final String  PH_X = "x";
    public static final String  PH_ERASEABLE = "erasable";
    public static final String  PH_MOVEABLE = "movable";

    //attributes for <ut> tag
    public static final String  UT_X = "x";
    public static final String  UT_ERASEABLE = "erasable";
    public static final String  UT_MOVEABLE = "movable";

    /*
        static final String  LOCTYPE = "locType";
        static final String  EXTRACT = "extract";
        static final String  ADD = "add";
        static final String  DELETE = "delete";
        static final String  ADDED = "added";
        static final String  DELETED = "deleted";
    */
    public static final String PRSROOT_VERSION= "version";
    public static final String PRSROOT_ID= "id";
    public static final String PRSROOT_LOCALE= "locale";
    public static final String RECORD_PROFILEID="recordProfileId";
    public static final String RECORD_SEQUECENUMBER="sequenceNumber";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TABLENAME  = "tableName";
    public static final String COLUMN_DATATYPE  = "dataType";
    public static final String COLUMN_MAXLENGTH  = "maxLength";
    public static final String COLUMN_RULEID = "ruleId";
    public static final String COLUMN_CONTENTMODE = "contentMode";
    public static final String CONTEXT_NUMROWS = "numRows";
    public static final String CONTEXT_NUMCOLS = "numCols";
    
    public static final String INTERNAL = "internal";
}
