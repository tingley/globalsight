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
public class ErrorChecker_fr extends ListResourceBundle 
{

    static final Object [][] contents =
    {
        { "ErrorMissingTags", "Les marqueurs obligatoires suivants sont manquants :\n\t{0}" },
        { "ErrorInvalidAdd", "Les marqueurs suivants ne peuvent pas \u00eatre ajout\u00e9s :\n\t{0}" },
        { "ErrorUnbalancedTags", "Les marqueurs suivants ne sont pas \u00e9quilibr\u00e9s :\n\t{0}"},
        { "MaxLengthMsg",  "Longueur maximale d\u00e9pass\u00e9e. Veuillez r\u00e9duire la longueur de la traduction."},
        { "invalidXMLCharacter", "[The segment contains an invalid control character (Unicode: {0}). The character position is shown below:\n\n{1}]"}
    };

    public Object[][] getContents() {return contents;}
}