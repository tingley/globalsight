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

package com.globalsight.everest.page.pageimport;

/**
 * This interface holds the constants used by TemplateGenerator.
 */
public interface TemplateGeneratorConstants
{
    //Constants for defining records saved in template_format table
    public final String NAME_TMPL_START   = "template_start";
    public final String NAME_TMPL_END     = "template_end";
    public final String NAME_RECORD_START = "record_start";
    public final String NAME_RECORD_END   = "record_end";

    public final String TMPL_TYPE_EXPORT   = "EXP";
    public final String TMPL_TYPE_STANDARD = "STD";
    public final String TMPL_TYPE_DETAIL   = "DTL";
    // TODO: must be added to database definition and toplink descriptor
    public final String TMPL_TYPE_PREVIEW  = "PRV";

    public final String SRC_TYPE_PRS  = "PRS";
    public final String SRC_TYPE_GXML = "GXML";

    //Html tags for Gxml Standard templates
    public final String GXML_DATAROW_ODD_START  = "<TR BGCOLOR=#FFFFFF>\n";
    public final String GXML_DATAROW_EVEN_START = "<TR BGCOLOR=#EEEEEE>\n";
    public final String GXML_DATAROW_END = "</TR>\n";

    public final String GXML_DATACOL1_START =
        "<TD WIDTH=20 ALIGN=CENTER VALIGN=TOP>" +
        "<SPAN CLASS=standardText>";
    public final String GXML_DATACOL1_END   = "</SPAN></TD>\n";

    public final String GXML_DATACOL2_START = "<TD WIDTH=100%>";
    public final String GXML_DATACOL2_END   = "</TD>\n";

    //Html tags for PRS Standard and detail templates
    public final String PRS_COLUMN_ROW_START  = "<TR>";
    public final String PRS_COLUMN_ROW_END    = "</TR>\n";
    public final String PRS_LABEL_COL_START   =
        "<TD NOWRAP VALIGN=TOP><SPAN CLASS=standardText>";
    public final String PRS_LABEL_COL_END     = "</SPAN></TD>\n";
    public final String PRS_CONTENT_COL_START = "<TD WIDTH=100%>";
    public final String PRS_CONTENT_COL_END   = "</TD>\n";
    public final String PRS_CONTENT_LIST_START = "<TD WIDTH=100%><TABLE><TR>";
    public final String PRS_CONTENT_LIST_MID   = "</TR><TR>";
    public final String PRS_CONTENT_LIST_END   = "</TR></TABLE></TD>\n";

    //Column content_mode attribute values
    public final String PRS_COLUMN_CONTENTMODE_TRANSLATABLE = "translatable";
    public final String PRS_COLUMN_CONTENTMODE_CONTEXUAL    = "contextual";
    public final String PRS_COLUMN_CONTENTMODE_INVISIBLE    = "invisible";
}
