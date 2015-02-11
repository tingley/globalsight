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
public class ErrorChecker_ja extends ListResourceBundle 
{

    static final Object [][] contents =
    {
        { "ErrorMissingTags", "\u6b21\u306e\u5fc5\u9808\u30bf\u30b0\u304c\u6b20\u3051\u3066\u3044\u307e\u3059:\n\t{0}" },
        { "ErrorInvalidAdd", "\u6b21\u306e\u30bf\u30b0\u306f\u8ffd\u52a0\u3067\u304d\u307e\u305b\u3093:\n\t{0}" },
        { "ErrorUnbalancedTags", "\u6b21\u306e\u30bf\u30b0\u306f\u4e0d\u5747\u8861\u3067\u3059:\n\t{0}"},
        { "MaxLengthMsg", "\u9577\u3055\u306e\u6700\u9ad8\u9650\u5ea6\u3092\u8d8a\u3048\u307e\u3057\u305f\u3002\u7ffb\u8a33\u306e\u9577\u3055\u3092\u77ed\u7e2e\u3057\u3066\u304f\u3060\u3055\u3044\u3002"},
        { "invalidXMLCharacter", "[The segment contains an invalid control character (Unicode: {0}). The character position is shown below:\n\n{1}]"}
    };

    public Object[][] getContents() {return contents;}
}