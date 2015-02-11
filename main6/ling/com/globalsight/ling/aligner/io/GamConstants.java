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


public interface GamConstants
{
    static final String ELEM_GAM = "gam";
    static final String ELEM_ALIGN = "align";
    static final String ELEM_ISOLATE = "isolate";
    static final String ELEM_REMOVE = "remove";

    static final String ATT_VERSION = "version";
    static final String ATT_SOURCE_TMX = "source-tmx";
    static final String ATT_TARGET_TMX = "target-tmx";
    static final String ATT_SOURCE = "source";
    static final String ATT_TARGET = "target";
    static final String ATT_ID = "id";
    static final String ATT_LOCALE = "locale";
    static final String ATT_APPROVED = "approved";

    static final String VALUE_Y = "Y";
    static final String VALUE_N = "N";

    static final String GAM_DTD
        = "<!ELEMENT " + ELEM_GAM + " ("
        + ELEM_ALIGN + "|" + ELEM_ISOLATE + "|" + ELEM_REMOVE + ")*>\n"
        + "<!ATTLIST " + ELEM_GAM + "\n"
        + "  " + ATT_VERSION + " CDATA #FIXED \"1.0\"\n"
        + "  " + ATT_SOURCE_TMX + " CDATA #REQUIRED\n"
        + "  " + ATT_TARGET_TMX + " CDATA #REQUIRED>\n"
        + "<!ELEMENT " + ELEM_ALIGN + " EMPTY>\n"
        + "<!ATTLIST " + ELEM_ALIGN + "\n"
        + "  " + ATT_SOURCE + " CDATA #REQUIRED\n"
        + "  " + ATT_TARGET + " CDATA #REQUIRED\n"
        + "  " + ATT_APPROVED + " (Y|N) #REQUIRED>\n"
        + "<!ELEMENT " + ELEM_REMOVE + " EMPTY>\n"
        + "<!ATTLIST " + ELEM_REMOVE + "\n"
        + "  " + ATT_ID + " CDATA #REQUIRED\n"
        + "  " + ATT_LOCALE + " (S|T) #REQUIRED>\n";

}
