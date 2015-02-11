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

import java.util.ListResourceBundle;

/**
* Contains all the segment level error messages that can occur in this release.
*/
public class ErrorChecker_de extends ListResourceBundle 
{

    static final Object [][] contents =
    {
        { "ErrorMissingTags", "Die folgenden obligatorischen Tags fehlen:\n\t{0}" },
        { "ErrorInvalidAdd", "Die folgenden Tags k\u00f6nnen nicht hinzugef\u00fcgt werden:\n\t{0}" },
        { "ErrorUnbalancedTags", "Die folgenden Tags sind unausgewogen:\n\t{0}"},
        { "MaxLengthMsg", "L\u00e4nge \u00fcberschritten. Bitte verk\u00fcrzen Sie die L\u00e4nge der \u00dcbersetzung."},
        { "invalidXMLCharacter", "[The segment contains an invalid control character (Unicode: {0}). The character position is shown below:\n\n{1}]"}
    };

    public Object[][] getContents() {return contents;}
}