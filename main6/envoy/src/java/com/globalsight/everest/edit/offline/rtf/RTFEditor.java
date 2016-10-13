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



package com.globalsight.everest.edit.offline.rtf;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileWriter;

/**
 * RTF code that defines the editor.
 */
public class RTFEditor
{
    //
    // Public Constants
    //

    /** A placeholder for additional RTF that is inserted on the client side. */
    static public final String PLACEHOLDER = "@@PLACEHOLDER@@";

    //
    // Private & Protected Constants
    //

    // NEW western european and asian editor 10-25-04

    /** starting portion of editor */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_START =
        "{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid14369035\\par {\\*\\bkmkstart editor}\\par \n";
    /** the sourc box - WITH PLACEHOLDER for source text RTF */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX =
        "{\\ltrpar\\ql \\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat3\\cbpat8 \n" +
        "\\rtlch\\fcs1 \\ltrch\\fcs0 \\f1\\fs20\\chshdng0\\chcfpat0\\chcbpat17\\insrsid14369035\n" +
        PLACEHOLDER + "\\par\n}" ;
    /** the info line - WITH PLACEHOLDER for info text RTF */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE =
        "\\li0\\ri0\\widctlpar\\faauto\\rin0\\lin0\\itap0 \n" +
        "{\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\insrsid14369035 \n" +
        PLACEHOLDER + " " + PLACEHOLDER + "  {\\*\\bkmkstart trgStart}{\\*\\bkmkend trgStart}\\par }\n";
    /** the target box - WITH PLACEHOLDER for target text RTF */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX =
        "\\pard \n" +
        "{\\ltrpar\\ql \\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \n" +
        "\\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat7\\cbpat8 \n" +
        "\\rtlch\\fcs1 \\ltrch\\fcs0 \\f1\\fs20\\insrsid14369035 \n" +
        PLACEHOLDER + "\\par}\n" ;
    /** ending portion of the editor */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_END =
        "\\pard {\\*\\bkmkend editor}}\n" ;

    /** rtf for the merge indicator */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR =
        "{\\rtlch\\fcs1 \\ltrch\\fcs0 \\fs18\\cf19\\insrsid14369035 " +  PLACEHOLDER + "}\n" ;
    /** rtf for the un-merged indicator */
    static private final String WESTERN_EUROPEAN_ASIAN_EDITOR_UNMERGE_INDICATOR =
        "{\\rtlch\\fcs1 \\ltrch\\fcs0 \\fs18\\cf19\\insrsid14369035 " + PLACEHOLDER + "}\\par\n";

    //  This is the original COMMON editor from previous releases
    //  which had troubles for Japanese due to the presence of the RTF
    //  token "\\dbch". We removed \\dbch, refactored groups {} and
    //  removed all the associated text attributes to create the new
    //  editor above (for western European & Asian langs). A separate
    //  dedicated list of RTF (below) is now used to create a
    //  dedicated editor for BIDI langs.

    //  Keeping this for reference in case of problems.
    // ====================================================
    //    private static final String SEG_EDITOR_RTF =
    //        "{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid14369035\\par {\\*\\bkmkstart editor}\\par }" +
    //        "\\pard \\ltrpar\\ql \\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 " +
    //        "\\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 " +
    //        "\\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 " +
    //        "\\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat3\\cbpat8 "+
    //        "{\\rtlch\\fcs1 \\af1\\afs20 \\ltrch\\fcs0 \\f1\\fs20\\chshdng0\\chcfpat0\\chcbpat17\\insrsid14369035 " +
    //        "{\\*\\bkmkstart src}\\hich\\af1\\dbch\\af13\\loch\\f1 Dummy source}" +
    //        "{\\rtlch\\fcs1 \\af0\\afs20 \\ltrch\\fcs0 " +
    //        "\\fs20\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkend src}" +
    //        "\\par }\\pard \\ltrpar\\ql \\li0\\ri0\\widctlpar\\faauto\\rin0\\lin0\\itap0 "+
    //        "{\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 "+
    //        "{\\*\\bkmkstart info}Dummy info{\\*\\bkmkend info}" +
    //        "{      \\*\\bkmkstart mergeIndicator}Dummy merge indicator{\\*\\bkmkend mergeIndicator}  " +
    //        "{\\*\\bkmkstart trgStart}{\\*\\bkmkend trgStart}" +
    //        "\\par }\\pard \\ltrpar\\ql \\li0\\ri0\\widctlpar\\brdrt\\brdrs" +
    //        "\\brdrw15\\brsp20\\brdrcf16 \\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 "+
    //        "\\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrr\\brdrs\\brdrw15" +
    //        "\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 " +
    //        "\\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat7\\cbpat8 " +
    //        "{\\rtlch\\fcs1 \\af1\\afs20 \\ltrch\\fcs0 \\f1\\fs20\\insrsid14369035 " +
    //        "{\\*\\bkmkstart trg}\\hich\\af1\\dbch\\af13\\loch\\f1  Dummy target }" +
    //        "{\\rtlch\\fcs1 \\af0\\afs20 \\ltrch\\fcs0 " +
    //        "\\fs20\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 " +
    //        "{\\*\\bkmkend trg}\\par }\\pard \\ltrpar\\ql \\li0\\ri0\\widctlpar " +
    //        "\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 " +
    //        "{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid15931563 " +
    //        "{\\*\\bkmkend editor}" +
    //        "{\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\cf19\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 \\par\\par {\\*\\bkmkstart mergeIndicatorMerged}Merged segments{\\*\\bkmkend mergeIndicatorMerged}}" +
    //        "{\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\cf19\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 \\par {\\*\\bkmkstart mergeIndicatorNotMerged}    {\\*\\bkmkend mergeIndicatorNotMerged}  <- note: this empty bookmarked text is the \"Not Merged\" Indicator value.}}\\par";
    //

    // NEW dedicated BIDI editor 10-25-04

    /** starting portion of editor */
    private static final String BIDI_EDITOR_START =
        "{{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid14369035\\par {\\*\\bkmkstart editor}\\par}" ;
    /** the sourc box - WITH PLACEHOLDER for source text RTF */
    private static final String BIDI_EDITOR_SRCBOX =
        "\\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16" +
        "\\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 " +
        "\\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\faauto\\rin0\\lin0\\rtlgutter\\itap0 " +
        "\\shading2500\\cfpat3\\cbpat8 {\\rtlch\\fcs1 \\af1\\afs20 \\ltrch\\fcs0 " +
        "\\f1\\fs20\\chshdng0\\chcfpat0\\chcbpat17\\insrsid14369035" + PLACEHOLDER + "\\par }\\pard ";
    /** the info line - WITH PLACEHOLDER for info text RTF */
    private static final String BIDI_EDITOR_INFOLINE =
        "\\li0\\ri0\\widctlpar\\faauto\\rin0\\lin0\\itap0 {\\qr\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 "+
        PLACEHOLDER + " " +  PLACEHOLDER + "{\\*\\bkmkstart trgStart}{\\*\\bkmkend trgStart}\\par}";
    /** the target box - WITH PLACEHOLDER for target text RTF */
    private static final String BIDI_EDITOR_TRGBOX =
        "\\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 "+
        "\\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 "+
        "\\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat7\\cbpat8 {\\rtlch\\fcs1"+
        "\\af1\\afs20\\ltrch\\fcs0\\f1\\fs20\\insrsid14369035" + PLACEHOLDER + "\\par}";
    /** ending portion of the editor */
    private static final String BIDI_EDITOR_END =
        "\\li0\\ri0\\widctlpar \\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 {\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid15931563 "+
        "{\\*\\bkmkend editor}}}";

    /** rtf for the merge indicator */
    private static final String BIDI_EDITOR_MERGE_INDICATOR =
        "{{\\qr\\rtlch\\fcs1 \\cf19 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 " +
        PLACEHOLDER + "}";

    /** rtf for the un-merged indicator */
    private static final String BIDI_EDITOR_UNMERGE_INDICATOR =
        "{\\qr\\rtlch\\fcs1 \\cf19 \\af0\\afs18 "+
        "\\\\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 " + PLACEHOLDER + "}\\par}";

    // After experimentation in MS-Word, this RTF was copied from the
    // raw (MS-Word generated) RTF file and sliced up to create the
    // above bidi editor.

    ////        "{{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid14369035\\par {\\*\\bkmkstart editor}\\par }" +
    ////        " \\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat3\\cbpat8 " +
    ////        "{\\rtlch\\fcs1 \\af1\\afs20 \\ltrch\\fcs0 \\f1\\fs20\\chshdng0\\chcfpat0\\chcbpat17\\insrsid14369035 {\\*\\bkmkstart src}\\hich\\af1\\dbch\\af13\\loch\\f1 Dummy source}" +
    ////        "{\\rtlch\\fcs1 \\af0\\afs20 \\ltrch\\fcs0 \\fs20\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkend src}\\par }\\pard" +
    ////        " \\li0\\ri0\\widctlpar\\faauto\\rin0\\lin0\\itap0 " +
    ////        "{\\qr\\rtlch\\fcs1 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkstart info}Dummy info{\\*\\bkmkend info}{      \\*\\bkmkstart mergeIndicator}Dummy merge indicator{\\*\\bkmkend mergeIndicator}{\\*\\bkmkstart trgStart}{\\*\\bkmkend trgStart}\\par }" +
    ////        " \\li0\\ri0\\widctlpar\\brdrt\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrl\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrb\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrr\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\brdrbtw\\brdrs\\brdrw15\\brsp20\\brdrcf16 \\faauto\\rin0\\lin0\\rtlgutter\\itap0 \\shading2500\\cfpat7\\cbpat8 " +
    ////        "{\\rtlch\\fcs1 \\af1\\afs20 \\ltrch\\fcs0 \\f1\\fs20\\insrsid14369035 {\\*\\bkmkstart trg}\\hich\\af1\\dbch\\af13\\loch\\f1  Dummy target }" +
    ////        "{\\qr\\rtlch\\fcs1 \\af0\\afs20 \\ltrch\\fcs0 \\fs20\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkend trg}\\par } " +
    ////        "\\li0\\ri0\\widctlpar \\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 " +
    ////        "{\\rtlch\\fcs1 \\af0 \\ltrch\\fcs0 \\insrsid15931563 {\\*\\bkmkend editor}}}" +
    ////        "{{\\par\\par \\qr\\rtlch\\fcs1 \\cf19 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkstart mergeIndicatorMerged}Merged segments{\\*\\bkmkend mergeIndicatorMerged}}" +
    ////        "{\\par \\qr\\rtlch\\fcs1 \\cf19 \\af0\\afs18 \\ltrch\\fcs0 \\fs18\\loch\\af1\\hich\\af1\\dbch\\af0\\insrsid14369035 {\\*\\bkmkstart mergeIndicatorNotMerged}    {\\*\\bkmkend mergeIndicatorNotMerged}  <- note: this empty bookmarked text is the \"Not Merged\" Indicator value.}\\par}";


    // WESTERN EUROPEAN ASIAN ------

    /**
     * Use with new binary resource file (though currently not used -
     * we get each editor part separatly)
     */
    private static final String COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_PLACHOLDERS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_START +
        WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX +
        WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE +
        WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX +
        WESTERN_EUROPEAN_ASIAN_EDITOR_END;

    /** Use with old rtf resource files **/
    private static final String COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_BOOKMARKS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_START  +
        WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart src}Dummy source {\\\\*\\\\bkmkend src}")  +
        WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart info}Dummy info{\\\\*\\\\bkmkend info}{      \\\\*\\\\bkmkstart mergeIndicator}Dummy merge indicator{\\\\*\\\\bkmkend mergeIndicator}") +
        WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart trg}Dummy target{\\\\*\\\\bkmkend trg}") +
        WESTERN_EUROPEAN_ASIAN_EDITOR_END;

    /** Use with old rtf resource files **/
    private static final String WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR_WITH_BOOKMARKS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR.replaceFirst(PLACEHOLDER,
            "{\\*\\bkmkstart mergeIndicatorMerged}Merged segments{\\*\\bkmkend mergeIndicatorMerged}");

    /** Use with new binary resource files **/
    private static final String WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR_WITH_PLACEHOLDERS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR;

    /** Use with old rtf resource files **/
    private static final String WESTERN_EUROPEAN_ASIAN_EDITOR_UNMERGE_INDICATOR_WITH_BOOKMARKS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_UNMERGE_INDICATOR.replaceFirst(PLACEHOLDER,
            "{\\*\\bkmkstart mergeIndicatorNotMerged}    {\\*\\bkmkend mergeIndicatorNotMerged}  <- note: this empty bookmarked text is the \"Not Merged\" Indicator value.");

    /** Use with new binary resource files **/
    private static final String WESTERN_EUROPEAN_ASIAN_EDITOR_UNMERGE_INDICATOR_WITH_PLACEHOLDERS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_UNMERGE_INDICATOR;

    // BIDI -------

    /** Use with new binary resource file
        (though currently not used - we get each editor part separatly) **/
    private static final String COMPLETE_BIDI_EDITOR_WITH_PLACHOLDERS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_START +
        WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX +
        WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE +
        WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX +
        WESTERN_EUROPEAN_ASIAN_EDITOR_END;

    /** Use with old rtf resource files **/
    private static final String COMPLETE_BIDI_EDITOR_WITH_BOOKMARKS =
        WESTERN_EUROPEAN_ASIAN_EDITOR_START +
        WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart src}\\\\hich\\\\af1\\\\dbch\\\\af13\\\\loch\\\\f1 Dummy source}{\\\\rtlch\\\\fcs1 \\\\af0\\\\afs20 \\\\ltrch\\\\fcs0 \\\\fs20\\\\loch\\\\af1\\\\hich\\\\af1\\\\dbch\\\\af0\\\\insrsid14369035 {\\\\*\\\\bkmkend src}")  +
        WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart info}Dummy info{\\\\*\\\\bkmkend info}{      \\\\*\\\\bkmkstart mergeIndicator}Dummy merge indicator{\\\\*\\\\bkmkend mergeIndicator}") +
        WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX.replaceFirst(PLACEHOLDER,
            "{\\\\*\\\\bkmkstart trg}\\\\hich\\\\af1\\\\dbch\\\\af13\\\\loch\\\\f1 Dummy target}{\\\\qr\\\\rtlch\\\\fcs1\\\\af0\\\\afs20\\\\ltrch\\\\fcs0\\\\fs20\\\\loch\\\\af1\\\\hich\\\\af1\\\\dbch\\\\af0\\\\insrsid14369035 {\\\\*\\\\bkmkend trg}") +
        WESTERN_EUROPEAN_ASIAN_EDITOR_END;

    /** Use with old rtf resource files **/
    private static final String BIDI_EDITOR_MERGE_INDICATOR_WITH_BOOKMARKS =
        BIDI_EDITOR_MERGE_INDICATOR.replaceFirst(PLACEHOLDER,
            "{\\*\\bkmkstart mergeIndicatorMerged}Merged segments{\\*\\bkmkend mergeIndicatorMerged}");

    /** Use with new binary resource files **/
    private static final String BIDI_EDITOR_MERGE_INDICATOR_WITH_PLACEHOLDERS =
        BIDI_EDITOR_MERGE_INDICATOR;

    /** Use with old rtf resource files **/
    private static final String BIDI_EDITOR_UNMERGE_INDICATOR_WITH_BOOKMARKS =
        BIDI_EDITOR_MERGE_INDICATOR.replaceFirst(PLACEHOLDER,
            "{\\*\\bkmkstart mergeIndicatorNotMerged}    "+
            "{\\*\\bkmkend mergeIndicatorNotMerged}  <- note: this empty bookmarked text is the \"Not Merged\" Indicator value.");

    /** Use with new binary resource files **/
    private static final String BIDI_EDITOR_UNMERGE_INDICATOR_WITH_PLACEHOLDERS =
        BIDI_EDITOR_UNMERGE_INDICATOR;


    //
    // Private Vaviables
    //

    private boolean m_rtlTrgLang = false;


    //
    // Constructors
    //

    public RTFEditor(boolean p_rtlTrgLang)
    {
        m_rtlTrgLang = p_rtlTrgLang;
    }


    //
    // Public methods
    //

    /**
     * Test code.
     */
    public static void main(String[] args)
    {
        // NOTE: this is not useful to debug the edtor. To do that, it
        //       is best to work with word and the raw RTF.
        //
        // 1 Download a live file and make a copy of it.
        // 2 Open the file in the editor and observe the editor behavior
        // 3 Close the file WITHOUT SAVING IT TO PRESERVE OUR DONWLOADED RTF
        // 4 make changes to the original Raw RTF
        // 5 repeat steps 2-5
        // 6 migrate the new RTF into the RTFEditor class (keep a copy
        // of the previous RTF) so if you loose the original or need to
        // compare, you can use the original COPY.
        //
        // Right now, this method simply prints out a dummy RTF file
        // to see if the editor was assembled correctly.  LCIDS are
        // not set.
        //
        doTest();
    }

    /**
     * For the old RTF resource files. Gets the original complete
     * editor with bookmarks.  (NOTE: we will be phasing this out.)
     */
    public String getCompleteEditorWithBookmarks()
    {
        return m_rtlTrgLang ? COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_BOOKMARKS :
            COMPLETE_BIDI_EDITOR_WITH_BOOKMARKS;
    }

    /** get the starting RTF that preceeds the source box**/
    public String getEditorStart()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_START : WESTERN_EUROPEAN_ASIAN_EDITOR_START;
    }

    /** get the RTF for the source box : includes placeholder for source text **/
    public String getSrcBox()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_SRCBOX : WESTERN_EUROPEAN_ASIAN_EDITOR_SRCBOX;
    }

    /** get the RTF for the info box : includes placeholder for info text **/
    public String getInfoBox()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_INFOLINE : WESTERN_EUROPEAN_ASIAN_EDITOR_INFOLINE;
    }

    /** get the RTF for the target box : includes placeholder for target text **/
    public String getTrgBox()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_TRGBOX : WESTERN_EUROPEAN_ASIAN_EDITOR_TRGBOX;
    }

    /** get the ending RTF that follows the target box **/
    public String getEditorEnd()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_END : WESTERN_EUROPEAN_ASIAN_EDITOR_END;
    }

    /**
     * get the RTF that defines the merge indicator : includes
     * placeholder for the merge indicator text
     */
    public String getMergeIndicator()
    {
        return m_rtlTrgLang ? BIDI_EDITOR_MERGE_INDICATOR :
            WESTERN_EUROPEAN_ASIAN_EDITOR_MERGE_INDICATOR;
    }

    //
    // Private Methods
    //

    private static void doTest()
    {
        try
        {
            String m_strEOL = "\r\n";

            // make rtf file header
            File f = File.createTempFile("GSRTFTest", "RTFEditor.rtf");
            FileWriter fw = new FileWriter(f);
            fw.write("{");
            //\\rtf1\\ansi\\ansicpg1252\\uc0\\deflang1033\\deflangfe1033\\deff0 ");
            fw.write(ParaViewWorkDocWriter.RTF_HEADER_START);

            fw.write(m_strEOL);
            fw.write(m_strEOL);
            fw.write(ParaViewWorkDocWriter.RTF_FONT_TABLE_START);
            //        fw.write("{\\f0" + str_sourceFont + "}");
            //        fw.write("{\\f1" + str_targetFont + "}");
            //        fw.write("{\\f2" + str_commentFont + "}");
            fw.write(ParaViewWorkDocWriter.RTF_FONT_TABLE_END);
            fw.write(m_strEOL);
            fw.write(m_strEOL);
            fw.write(ParaViewWorkDocWriter.COLOR_TABLE);
            fw.write(m_strEOL);
            fw.write(m_strEOL);
            fw.write(ParaViewWorkDocWriter.RTF_STYLESHEET_START);
            // default document style ("Normal") is in target language,
            // so you can hit ^space (normal style) and start overwriting
            // text in the target language
            //        fw.write("\\lang" + m_iTargetLcid +
            //            "\\langfe" + m_iTargetLcid +
            //            "\\langnp" + m_iTargetLcid +
            //            "\\langfenp" + m_iTargetLcid);
            fw.write(ParaViewWorkDocWriter.RTF_STYLESHEET_END);
            fw.write(m_strEOL);
            fw.write(m_strEOL);

            // write each editor
            fw.write("COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_PLACHOLDERS");
            fw.write(COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_PLACHOLDERS);
            fw.write("\\par\\par ");

            fw.write("COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_BOOKMARKS ");
            fw.write("NOTE: bookmarks will only appear in MS-Word once for last editor)");
            fw.write(COMPLETE_WESTERN_EUROPEAN_ASIAN_EDITOR_WITH_BOOKMARKS);
            fw.write("\\par\\par ");

            fw.write("COMPLETE_BIDI_EDITOR_WITH_PLACHOLDERS");
            fw.write(COMPLETE_BIDI_EDITOR_WITH_PLACHOLDERS);
            fw.write("\\par\\par ");

            fw.write("COMPLETE_BIDI_EDITOR_WITH_BOOKMARKS");
            fw.write(COMPLETE_BIDI_EDITOR_WITH_BOOKMARKS);
            fw.write("\\par\\par ");

            fw.write(" \\par}");
            fw.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }
}
