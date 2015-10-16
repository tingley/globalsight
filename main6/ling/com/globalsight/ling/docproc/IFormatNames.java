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

/**
 * An interface class to provide file format names. Inherit from it and you
 * don't have to qualify the names anymore.
 */
public interface IFormatNames
{
    public static final String FORMAT_PLAINTEXT = "plaintext";
    public static final String FORMAT_HTML = "html";
    public static final String FORMAT_JHTML = "jhtml";
    public static final String FORMAT_JAVASCRIPT = "javascript";
    public static final String FORMAT_JAVA = "java";
    public static final String FORMAT_CSS = "css";
    public static final String FORMAT_CSS_STYLE = "css-styles";
    public static final String FORMAT_XML = "xml";
    public static final String FORMAT_RESX = "resx";
    public static final String FORMAT_XSL = "xsl";
    public static final String FORMAT_JAVAPROP = "javaprop";
    public static final String FORMAT_JAVAPROP_HTML = "javaprop-html";
    public static final String FORMAT_JAVAPROP_MSG = "javaprop-msg";
    public static final String FORMAT_VBSCRIPT = "vbscript";
    public static final String FORMAT_CFSCRIPT = "cfscript";
    public static final String FORMAT_CF = "cfm";
    public static final String FORMAT_ASP = "asp";
    public static final String FORMAT_JSP = "jsp";
    public static final String FORMAT_PHP = "php";
    public static final String FORMAT_CPP = "cpp";
    public static final String FORMAT_RTF = "rtf";
    public static final String FORMAT_SGML = "sgml";
    public static final String FORMAT_XPTAG = "xptag";
    public static final String FORMAT_TROFF_MAN = "troff-man";

    // Office Extractor support
    public static final String FORMAT_EXCEL_HTML = "excel-html";
    public static final String FORMAT_WORD_HTML = "word-html";
    public static final String FORMAT_POWERPOINT_HTML = "powerpoint-html";

    // Office(Open Document Format) Extractor support
    public static final String FORMAT_OFFICE_XML = "office-xml";

    // HTML + "plumber" preprocessor
    public static final String FORMAT_EBAY_SGML = "ebay-sgml";

    // XML + XML decl preprocessor
    public static final String FORMAT_EBAY_PRJ = "ebay-prj";

    public static final String FORMAT_XLIFF = "xlf";
    public static final String FORMAT_XLIFF20 = "xlf2.0";
    public static final String FORMAT_XLIFF_NAME = "Xliff";

    public static final String FORMAT_OPENOFFICE_XML = "openoffice-xml";
    public static final String FORMAT_RC = "rc";
    public static final String FORMAT_PO = "po";
    public static final String FORMAT_TDA = "TDA";
    public static final String FORMAT_MIF = "mif";
    public static final String FORMAT_IDML = "idml";
    public static final String FORMAT_PASSOLO = "passolo";
    public static final String FORMAT_WINDOWSPE = "windows_pe";
    public static final String FORMAT_AUTHORIT_XML = "authorIT-xml";
}
