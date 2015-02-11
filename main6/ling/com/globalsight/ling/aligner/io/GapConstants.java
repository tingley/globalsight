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
package com.globalsight.ling.aligner.io;


public interface GapConstants
{
    static final String ELEM_GAP = "gap";
    static final String ELEM_FILES = "files";

    static final String ATT_VERSION = "version";
    static final String ATT_SOURCE_LOCALE = "source-locale";
    static final String ATT_TARGET_LOCALE = "target-locale";
    static final String ATT_ORIGINAL_SOURCE_FILE = "original-source-file";
    static final String ATT_ORIGINAL_TARGET_FILE = "original-target-file";
    static final String ATT_SOURCE_CUV_ID = "source-cuv-id";
    static final String ATT_TARGET_CUV_ID = "target-cuv-id";
    static final String ATT_SOURCE_TMX = "source-tmx";
    static final String ATT_TARGET_TMX = "target-tmx";
    static final String ATT_GAM = "gam";
    static final String ATT_STATE = "state";

    static final String GAP_DTD
        = "<!ELEMENT " + ELEM_GAP + " (" + ELEM_FILES + ")+>\n"
        + "<!ATTLIST " + ELEM_GAP + "\n"
        + "  " + ATT_VERSION + " CDATA #FIXED \"1.0\"\n"
        + "  " + ATT_SOURCE_LOCALE + " CDATA #REQUIRED\n"
        + "  " + ATT_TARGET_LOCALE + " CDATA #REQUIRED>\n"
        + "<!ELEMENT " + ELEM_FILES + " EMPTY>\n"
        + "<!ATTLIST " + ELEM_FILES + "\n"
        + "  " + ATT_ORIGINAL_SOURCE_FILE + " CDATA #REQUIRED\n"
        + "  " + ATT_ORIGINAL_TARGET_FILE + " CDATA #REQUIRED\n"
        + "  " + ATT_SOURCE_CUV_ID + " CDATA #IMPLIED\n"
        + "  " + ATT_TARGET_CUV_ID + " CDATA #IMPLIED\n"
        + "  " + ATT_SOURCE_TMX + " CDATA #REQUIRED\n"
        + "  " + ATT_TARGET_TMX + " CDATA #REQUIRED\n"
        + "  " + ATT_GAM + " CDATA #REQUIRED\n"
        + "  " + ATT_STATE + " CDATA #REQUIRED>\n";

}
