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

package galign.helpers.tmx;

/**
 * This class provides element and attribute names for the TMX DTD in
 * proper spelling and case, as well as version and DTD constants.
 *
 * (Extracted from com.globalsight.everest.tm.util.)
 */
public interface TmxConstants
{
    //
    // TMX version constants
    //

    static public final String TMX_11 = "1.1";
    static public final String TMX_12 = "1.2";
    static public final String TMX_13 = "1.3";
    static public final String TMX_14 = "1.4";
    static public final String TMX_DTD_11 = "tmx11.dtd";
    static public final String TMX_DTD_12 = "tmx12.dtd";
    static public final String TMX_DTD_13 = "tmx13.dtd";
    static public final String TMX_DTD_14 = "tmx14.dtd";
    static public final int TMX_VERSION_11 = 110;
    static public final int TMX_VERSION_12 = 120;
    static public final int TMX_VERSION_13 = 130;
    static public final int TMX_VERSION_14 = 140;

    // GlobalSight's version, used as backup format
    static public final String TMX_GS = "1.0 GS";
    static public final String TMX_DTD_GS = "tmx-gs.dtd";
    static public final int TMX_VERSION_GS = 42;

    //
    // Constants for TMX element and attribute names.
    //
    static final public String ADMINLANG = "adminlang";
    static final public String CHANGEDATE = "changedate";
    static final public String CHANGEID = "changeid";
    static final public String CREATIONDATE = "creationdate";
    static final public String CREATIONID = "creationid";
    static final public String CREATIONTOOL = "creationtool";
    static final public String CREATIONTOOLVERSION = "creationtoolversion";
    static final public String DATATYPE = "datatype";
    static final public String LASTUSAGEDATE = "lastusagedate";
    static final public String O_ENCODING = "o-encoding";
    static final public String O_TMF = "o-tmf";
    static final public String SEGTYPE = "segtype";
    static final public String SRCLANG = "srclang";
    static final public String TUID = "tuid";
    static final public String USAGECOUNT = "usagecount";
    static final public String VERSION = "version";

    // careful: must write "xml:lang" but read "lang"
    static final public String LANG = "lang";

    //
    // User-defined constants we use in <prop> when writing out GXML
    // in TMX (aka, G-TMX)
    //
    static final public String PROP_SEGMENTTYPE = "gs-segment-type";

    static final public String PROP_TUTYPE = "gs-tu-type";
    static final public String VAL_TU_LOCALIZABLE = "localizable";
    static final public String VAL_TU_TRANSLATABLE = "translatable";


    //
    // System-defined constants when writing out the TMX header
    //
    static final public String GLOBALSIGHT = "Globalsight";
    static final public String GLOBALSIGHTVERSION = "6.0";

    // segtype values
    static final public String SEGMENTATION_BLOCK = "block";
    static final public String SEGMENTATION_PARAGRAPH = "paragraph";
    static final public String SEGMENTATION_SENTENCE = "sentence";
    static final public String SEGMENTATION_PHRASE = "phrase";

    // o-tmf values: our pivot is GXML, so this is our original TM format
    static final public String TMF_GXML = "gxml";

    // adminlang values: en_US by default
    static final public String DEFAULT_ADMINLANG = "EN-US";

    // datatype values: pivot is HTML, so all segments are HTML by default
    static final public String DATATYPE_HTML = "html";

    // srclang values: en_US by default
    static final public String DEFAULT_SOURCELANG = "EN-US";

    // default user that created a TU/TUV if information is missing.
    static final public String DEFAULT_USER = "system";
}
