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
* Contains ptag mapping table alternate messages.
*/
public class Ptag_ja extends ListResourceBundle 
{

    public Object[][] getContents() {return contents;}

    static final Object [][] contents =
    {
        {"NativeAlternateForSpace", "\u5358\u4e00\u30b9\u30da\u30fc\u30b9" },
        {"NativeAlternateForTab", "\u5358\u4e00\u30bf\u30d6" },
        {"NativeAlternateForFF", "\u5358\u4e00\u30d5\u30a9\u30fc\u30e0 \u30d5\u30a3\u30fc\u30c9" },
        {"NativeAlternateForLB", "\u5358\u4e00\u30e9\u30a4\u30f3\u30d6\u30ec\u30fc\u30af" }
    };
}