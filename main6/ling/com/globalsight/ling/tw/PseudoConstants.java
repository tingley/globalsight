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
package com.globalsight.ling.tw;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

/**
 * Contants used by the pseudo tag covnersion classes.
 */
public interface PseudoConstants
{   
    // GENERAL    
    /** The character that starts a Psuedo tag. */
    public static final char PSEUDO_OPEN_TAG = '[';
    /** The character that ends a Psuedo tag. */
    public static final char PSEUDO_CLOSE_TAG = ']';
    /** The character that denotes a Psuedo end-tag. */
    public static final char PSEUDO_END_TAG_MARKER = '/';
    /** The delimter used to escape the PSEUDO_OPEN_TAG character. */
    public static final char PSEUDO_TAG_ESCAPE = '[';
    /** The path to property resource file */
    public static final String PSEUDO_RESPATH = "com.globalsight.ling.tw.Ptag";

    // PTAG MODES
    /** Indcates verbose naming convention. */
    public static final int PSEUDO_VERBOSE = 1;
    /** Indcates compact naming convention. */
    public static final int PSEUDO_COMPACT = 2;
    /** Indcates native-tmx naming convention. */
    public static final int PSEUDO_TMXNAME = 3;

    // ADDABLE MODES
    /** ID to disable addables*/
    public static final int ADDABLES_DISABLED = -1;
    /** ID to enable HTML addables*/
    public static final int ADDABLES_AS_HTML = 0;
    /** ID to enable RTF addables*/
    public static final int ADDABLES_AS_RTF = 1;
    /** ID to enable RTF addables*/
    public static final int ADDABLES_AS_XLF = 2;
    /** ID to enable office 2010*/
    public static final int ADDABLES_AS_OFFICE_2010 = 3;
    /** ID to enable mif*/
    public static final int ADDABLES_AS_MIF = 4;
    

    // ADDABLES
    // Hash keys to common data
    /** Hask key to retrieve an addables tmx tag*/
    public static final String ADDABLE_TMX_TAG = "tmxtag";
    /** Hask key to retrieve an addables tmx types*/
    public static final String ADDABLE_TMX_TYPE = "tmxtype";
    /** Hask key to retrieve an addables erasable attr value*/
    public static final String ADDABLE_ATTR_ERASABLE = "erasable";
    /** Hask key to retrieve an addables movable attr value*/
    public static final String ADDABLE_ATTR_MOVABLE = "movable";
    /** Hask key to retrieve a paired addables tmx end tag*/
    public static final String ADDABLE_TMX_ENDPAIRTAG = "tmx-ept-tag";

    // ADDABLES
    // Hash keys to HTML specific data
    /** Hask key to retrieve HTML content for an addable tag*/
    public static final String ADDABLE_HTML_CONTENT = "html-content";
    /** Hask key to retrieve HTML content for an addable ept*/
    public static final String ADDABLE_ENDPAIR_HTML_CONTENT ="ept-html-content";
    /** Hask key to retrieve RTF content for an addable tag*/
    public static final String ADDABLE_RTF_CONTENT = "rtf-content";
    /** Hask key to retrieve RTF content for an addable ept*/
    public static final String ADDABLE_ENDPAIR_RTF_CONTENT ="ept-rtf-content";


 }