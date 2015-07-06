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
package com.globalsight.ling.common;

public interface DiplomatNames
{
    public interface Element
    {
        String DIPLOMAT = "diplomat";
        String TRANSLATABLE = "translatable";
        String SEGMENT = "segment";
        String LOCALIZABLE = "localizable";
        String SKELETON = "skeleton";
        String GSA = "gs";

        String BPT = "bpt";
        String EPT = "ept";
        String SUB = "sub";
        String IT  = "it";
        String PH  = "ph";
        String UT  = "ut";
    }


    public interface Attribute
    {
        String VERSION = "version";
        String LOCALE = "locale";
        String DATATYPE = "datatype";
        String TYPE = "type";
        String LOCTYPE = "locType";
        String TARGETENCODING = "targetEncoding";
        String BLOCKID = "blockId";
        String SEGMENTID = "segmentId";
        String WORDCOUNT = "wordcount";
        String PRESERVEWHITESPACE = "preserveWhiteSpace";
        String EXTRACT = "extract";
        String DESCRIPTION = "description";
        String ADD = "add";
        String DELETE = "delete";
        String ADDED = "added";
        String DELETED = "deleted";
        String NAME = "name";
        String ID = "id";
        String TARGETLANGUAGE = "targetLanguage";
        String ISLOCALIZED = "isLocalized";
        String ISAUTOCOMMITETM = "isAutoCommitTM";
        String INDDPAGENUM ="inddPageNum";
        String ESCAPINGCHARS = "escapingChars";
    }
}
