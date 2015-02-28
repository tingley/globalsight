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
package com.globalsight.cxe.entity.filterconfiguration;

public interface XmlFilterConstants
{
    public static final String NODE_ROOT = "xmlFilterConfig";
    public static final String NODE_EXTENDED_WHITESPACE_CHARS = "extendedWhitespaceChars";
    public static final String NODE_PH_CONSOLIDATION = "phConsolidation";
    public static final String NODE_PH_TRIM = "phTrim";
    public static final String NODE_NON_ASCII_AS = "nonasciiAs";
    public static final String NODE_WHITESPACE_HANDLE = "whitespaceHandleMode";
    public static final String NODE_ELEMENT_POST_FILTER = "elementPostFilter";
    public static final String NODE_ELEMENT_POST_FILTER_ID = "elementPostFilterId";
    public static final String NODE_CDATA_POST_FILTER = "cdataPostFilter";
    public static final String NODE_CDATA_POST_FILTER_ID = "cdataPostFilterId";
    public static final String NODE_SID_TAG_NAME = "sidTagName";
    public static final String NODE_SID_ATTR_NAME = "sidAttrName";
    public static final String NODE_IS_CHECK_WELL_FORMED = "isCheckWellFormed";
    public static final String NODE_IS_GENERATE_LANG = "isGerateLangInfo";
    public static final String NODE_WHITESPACE_PRESERVE_TAGS = "whitespacePreserveTags";
    public static final String NODE_EMBEDDED_TAGS = "embeddedTags";
    public static final String NODE_TRANSLATE_ATTRIBUTE_TAGS = "transAttrTags";
    public static final String NODE_CONTENT_INCLUTION_TAGS = "contentInclTags";
    public static final String NODE_EMPTY_TAG_FORMAT = "emptyTagFormat";
    public static final String NODE_CDATA_POST_FILTER_TAGS = "cdataPostfilterTags";
    public static final String NODE_ENTITIES = "entities";
    public static final String NODE_PROCESS_INS = "processIns";
    public static final String NODE_INTERNAL_TAG = "internalTag";
    public static final String NODE_SRCCMT_XMLCOMMENT = "srcCmtXmlComment";
    public static final String NODE_SRCCMT_XMLTAG = "srcCmtXmlTag";

    public static final String nullConfigXml = "<" + NODE_ROOT + ">" + "</"
            + NODE_ROOT + ">";

    // xml filter config values
    public static final int PH_CONSOLIDATE_DONOT = 1;
    public static final int PH_CONSOLIDATE_ADJACENT = 2;
    public static final int PH_CONSOLIDATE_ADJACENT_IGNORE_SPACE = 3;

    public static final int PH_TRIM_DONOT = 1;
    public static final int PH_TRIM_DO = 2;

    public static final int NON_ASCII_AS_CHARACTER = 1;
    public static final int NON_ASCII_AS_ENTITY = 2;

    public static final int WHITESPACE_HANDLE_COLLAPSE = 1;
    public static final int WHITESPACE_HANDLE_PRESERVE = 2;

    public static final int EMPTY_TAG_FORMAT_PRESERVE = 0;
    public static final int EMPTY_TAG_FORMAT_OPEN = 1;
    public static final int EMPTY_TAG_FORMAT_CLOSE = 2;

    public static final int ENTITY_TEXT = 0;
    public static final int ENTITY_PLACEHOLDER = 1;

    public static final int ENTITY_SAVE_AS_ENTITY = 0;
    public static final int ENTITY_SAVE_AS_CHAR = 1;

    public static final int PI_MARKUP = 0;
    public static final int PI_MARKUP_EMB = 1;
    public static final int PI_REMOVE = 2;
    public static final int PI_TRANSLATE = 3;
}
