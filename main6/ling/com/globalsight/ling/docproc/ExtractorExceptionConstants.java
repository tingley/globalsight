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
package com.globalsight.ling.docproc;


// TODO JEH 1/18/2000
// We need to add all possible exception types here.

/**
 * Lists the exception types that the extractor package can throw.
 */
public interface ExtractorExceptionConstants
{
    /**
     * Dummy id for all EF exceptions (value -1).
     * @deprecated as hell, please assign a meaningful error code
     */
    public static final int DUMMY                           =     -1;

    //
    // General Exceptions
    //

    /** Indicates a "should never happen error" (value -400). */
    public static final int INTERNAL_ERROR                  =   -400;
    /** Indicates an invalid IANA or Java encoding (value -401). */
    public static final int INVALID_ENCODING                =   -401;
    /** Indicates an invalid or non-existing source file (value -402). */
    public static final int INVALID_SOURCE                  =   -402;
    /** Indicates an invalid locale (value -403). */
    public static final int INVALID_LOCALE                  =   -403;
    /**
     * Indicates an invalid file format (value -404).
     * For the list of valid file formats, see {@link
     * com.globalsight.ling.docproc.ExtractorRegistry
     * ExtractorRegistry}.
    */
    public static final int INVALID_FILE_FORMAT             =   -404;

    //TODO: this one needs to be INTERNAL_ERROR
    /** Indicates a regular expression error (value -405). */
    public static final int REGEX_ERROR                     =   -405;
    /** Indicates a Diplomat XML parse error (value -406). */
    public static final int DIPLOMAT_XML_PARSE_ERROR        =   -406;

    /**
     * Indicates an invalid XML character in output (value -407).
     * Invalid characters according to the XML standard are all
     * control characters (range 0-32).  Only \t (0x9), \n (0xa), \r
     * (0xd) and graphic unicode chars (>= space, 0x20) are allowed.
     */
    public static final int INVALID_XML_CHAR                 =  -407;


    //
    // General Extractor Exceptions
    //
    /**
     * Indicates that the ExtractorRegistry singleton is not
     * available (value -410).  This error should never occur.
     */
    public static final int REGISTRY_NOT_AVAILABLE          =   -410;
    /**
     * Indicates a file format string is not registered (value -411).
     * For the list of valid file formats, see {@link
     * com.globalsight.ling.docproc.ExtractorRegistry
     * ExtractorRegistry}.
     */
    public static final int FORMAT_NOT_REGISTERED           =   -411;
    /**
     * Indicates an extractor can not be called as an embedded
     * extractor (value -412).
     */
    public static final int EMBEDDING_NOT_SUPPORTED         =   -412;

    //
    // Word Counter Exceptions
    //
    /** Indicates an error in the WordCounter (value -420). */
    public static final int WORD_COUNTER_ERROR              =   -420;

    //
    // HTML extractor
    //
    /** Indicates a parse error in the HTML parser (value -450). */
    public static final int HTML_PARSE_ERROR                =   -450;
    /** Indicates a unexpected error in the HTML parser (value -451). */
    public static final int HTML_UNEXPECTED_ERROR           =   -451;
    /** Indicates an error in the embedded JavaScript Parser (value -452). */
    public static final int HTML_EMBEDDED_JS_ERROR          =   -452;
    /** Indicates an error in the embedded CSS Parser (value -453). */
    public static final int HTML_EMBEDDED_CSS_ERROR         =   -453;
    /** Indicates an error in the embedded XML Parser (value -454). */
    public static final int HTML_EMBEDDED_XML_ERROR         =   -454;
    /** Indicates an error in the embedded CFScript Parser (value -455). */
    public static final int HTML_EMBEDDED_CF_ERROR          =   -455;
    /** Indicates an error in the embedded Java Parser (value -456). */
    public static final int HTML_EMBEDDED_JAVA_ERROR        =   -456;
    /** Indicates an error in the embedded VB Parser (value -457). */
    public static final int HTML_EMBEDDED_VB_ERROR          =   -457;
    /** Indicates an error in the embedded VB Parser (value -458). */
    public static final int HTML_GS_TAG_ERROR               =   -458;

    //
    // XML extractor
    //
    /** Indicates an internal error in the XML extractor (value -500). */
    public static final int XML_EXTRACTOR_INTERNAL_ERROR    =   -500;
    /** Indicates an extractor error in the XML extractor (value -501). */
    public static final int XML_EXTRACTOR_ERROR             =   -501;
    /**
     * Indicates a "not yet unsupported" error in the XML extractor
     * (value -502).
     */
    public static final int XML_EXTRACTOR_UNSUPPORTED_YET   =   -502;
    /** Indicates an unexpected text error in the XML extractor (value -503). */
    public static final int XML_EXTRACTOR_UNEXPECTED_TEXT   =   -503;
    /** Indicates an error in the XML rules file (value -504). */
    public static final int XML_EXTRACTOR_RULES_ERROR       =   -504;
    /** Indicates an unknown format error in the XML extractor (value -505). */
    public static final int XML_EXTRACTOR_UNKNOWN_FORMAT    =   -505;

    //
    // JavaProp extractor
    //
    /** Indicates an extractor error in the JavaProp extractor (value -550). */
    public static final int JP_EXTRACTOR_ERROR              =   -550;
    /**
     * Indicates an escape sequence decoding error in the JavaProp
     * extractor (value -551).
     */
    public static final int JP_ESCAPE_DECODE_ERROR          =   -551;
    /**
     * Indicates an extractor in a conditional extraction comment in
     * the JavaProp extractor (value -552).
     */
    public static final int JP_COND_EXTRACT_ERROR           =   -552;

    //
    // Javascript extractor
    //
    /** Indicates a parse error in the JavaScript parser (value -600). */
    public static final int JS_PARSE_ERROR                  =   -600;

    //
    // CSS extractor
    //
    /** Indicates a parse error in the CSS parser (value -650). */
    public static final int CSS_PARSE_ERROR                 =   -650;

    //
    // CPP extractor
    //
    /** Indicates a parse error in the C++ parser (value -670). */
    public static final int CPP_PARSE_ERROR                 =   -670;

    //
    // RTF extractor
    //
    /** Indicates a parse error in the RTF parser (value -680). */
    public static final int RTF_PARSE_ERROR                 =   -680;

    //
    // Diplomat Merger
    //
    /** Indicates a Merger error (value -700). */
    public static final int DIPLOMAT_MERGER_FATAL_ERROR      =  -700;
    /** Indicates a Diplomat XML parse error in the Merger (value -701). */
    public static final int DIPLOMAT_BASIC_PARSER_EXCEPTION  =  -701;

    //
    // PaginatedResultSetXml extractor
    //
    /**
     * Indicates a parse error in the PaginatedResultSet Parser
     * (value -750).
     */
    public static final int PAGINATED_PARSE_ERROR           =   -750;

    //
    // VBScript extractor
    //
    /** Indicates a parse error in the VBScript Script parser (value -800). */
    public static final int VB_PARSE_ERROR                  =   -800;

    //
    // CFScript extractor
    //
    /** Indicates a parse error in the ColdFusion Script parser (value -850). */
    public static final int CF_PARSE_ERROR                  =   -850;

    //
    // Java extractor
    //
    /** Indicates a parse error in the Java parser (value -900). */
    public static final int JAVA_PARSE_ERROR                =   -900;

    //
    // XPTAG extractor
    //
    /** Indicates a parse error in the XPTag parser (value -950). */
    public static final int XPTAG_PARSE_ERROR               =   -950;

    //
    // TROFF extractor
    //
    /** Indicates a parse error in the Troff parser (value -1000). */
    public static final int TROFF_PARSE_ERROR               =   -1000;

    //
    // SGML extractor
    //
    /** Indicates a parse error in the SGML parser (value -1050). */
    public static final int SGML_PARSE_ERROR                =   -1050;
    /** Indicates a unexpected error in the HTML parser (value -1051). */
    public static final int SGML_UNEXPECTED_ERROR           =   -1051;
    /** Indicates an error in the embedded JavaScript Parser (value -1052). */
    public static final int SGML_EMBEDDED_JS_ERROR          =   -1052;
    /** Indicates an error in the embedded CSS Parser (value -1053). */
    public static final int SGML_EMBEDDED_CSS_ERROR         =   -1053;
    /** Indicates an error in the embedded XML Parser (value -1054). */
    public static final int SGML_EMBEDDED_XML_ERROR         =   -1054;
    /** Indicates an error in the embedded Java Parser (value -1055). */
    public static final int SGML_EMBEDDED_JAVA_ERROR        =   -1055;
    /** Indicates an error in the embedded VB Parser (value -1056). */
    public static final int SGML_EMBEDDED_VB_ERROR          =   -1056;

    //RC extractor
    public static final int RC_PARSE_ERROR               =   -1100;
    
    //MIF extractor
    public static final int MIF_PARSE_ERROR               =   -1101;
    public static final int MIF_VERSION_ERROR               =   -1102;
    
    //
    // error messages
    //
    public static final String UNKNOWN_FORMAT  = "Unknown data format: ";

}
